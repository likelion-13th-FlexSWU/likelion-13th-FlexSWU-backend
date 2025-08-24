package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.missionDTO.MissionResponseDTO;
import com.flexswu.flexswu.dto.reviewDTO.OcrDataDTO;
import com.flexswu.flexswu.entity.*;
import com.flexswu.flexswu.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final ReviewRepository reviewRepository;
    private final RecommendRepository recommendRepository;
    private final RegionScoreRepository regionScoreRepository;
    private final UserScoreMonthRepository userScoreMonthRepository;
    private final MissionAuthenticationRepository missionAuthenticationRepository;

    private static final long SUMMER_VACATION_MISSION_ID = 1L;
    private static final long AI_VISIT_MISSION_ID = 2L;
    private static final long LUNCH_VISIT_MISSION_ID = 3L;
    private static final long SPEND_15000_MISSION_ID = 4L;
    private static final long SPEND_20000_MISSION_ID = 5L;


    @Transactional
    public String authenticateMission(Long userId, OcrDataDTO ocrData) {
        // 1. 필수 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        Mission mission = missionRepository.findById(ocrData.getMissionId())
                .orElseThrow(() -> new EntityNotFoundException("미션을 찾을 수 없습니다. ID: " + ocrData.getMissionId()));

        // 해커톤 기간 내에 동일한 미션을 이미 인증했는지 확인하는 로직을 추가
        // 1-1. 해커톤 기간 내 미션 중복 인증 방지
        missionAuthenticationRepository.findByUserAndMission(user, mission).ifPresent(auth -> {
            // 해커톤 기간을 하드코딩으로 정의합니다.
            LocalDate hackathonStart = LocalDate.of(2025, 8, 20);
            LocalDate hackathonEnd = LocalDate.of(2025, 8, 27);

            // 기존 인증 기록의 날짜를 가져옴
            LocalDate authDate = auth.getCreatedAt().toLocalDate();

            // 기존 인증 날짜가 해커톤 기간 내에 있는지 확인
            if (!authDate.isBefore(hackathonStart) && !authDate.isAfter(hackathonEnd)) {
                // 기간 내에 있다면, 예외를 발생시켜 중복 인증을 막음
                throw new IllegalStateException("기간 내에 이미 인증한 미션입니다.");
            }
        });

        // 사용자의 가장 최근 추천 장소(Recommend) 조회
        Recommend latestRecommend = recommendRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalStateException("추천받은 장소 기록이 없습니다."));

        LocalDateTime latestTime = latestRecommend.getCreatedAt(); //추천 생성 시간으로 추천 리스트들 불러옴
        List<Recommend> latestRecommends = recommendRepository.findAllByUserAndCreatedAt(user, latestTime);

        // 2. 장소 일치 여부 확인 (도로명 주소 우선, 다음으로 지번, 없으면 전화번호로) 추천 리스트들을 돌면서 비교
        // OCR의 'address'는 Recommend의 'roadAddress'와 비교
//        boolean isPlaceMatch = Objects.equals(ocrData.getAddress(), latestRecommend.getRoadAddress()) ||
//                Objects.equals(ocrData.getAddress(), latestRecommend.getAddress()) ||
//                Objects.equals(ocrData.getPhoneNum(), latestRecommend.getPhoneNum());
        // ocr 데이터와 일치한 추천 장소 > 리스트에 일치한 정보가 없으면 에러처리
        Recommend matchedRecommend = latestRecommends.stream()
                .filter(r ->
                        Objects.equals(ocrData.getAddress(), r.getRoadAddress()) ||
                                Objects.equals(ocrData.getAddress(), r.getAddress()) ||
                                Objects.equals(ocrData.getPhoneNum(), r.getPhoneNum())
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("OCR 데이터와 일치하는 추천 장소가 없습니다."));

        // 2-1. 장소 기준 중복 인증 확인
        if (missionAuthenticationRepository.existsByUserAndRecommend(user, matchedRecommend)) {
            throw new IllegalStateException("이미 이 장소로 다른 미션을 인증했습니다.");
        }

        // 3. 미션별 세부 조건 확인
        if (!isMissionConditionSatisfied(ocrData.getMissionId(), ocrData)) {
            throw new IllegalArgumentException("미션 세부 조건을 만족하지 못했습니다.");
        }

        // 4. 점수 반영 및 인증 기록 저장
        int score = mission.getScore();

        // 4-1. 사용자 총점 업데이트
        user.setTotalScore(user.getTotalScore() + score);

        // 4-2. 지역별 점수 업데이트
        RegionScore regionScore = regionScoreRepository.findBySidoAndGugun(user.getSido(), user.getGugun())
                .orElseThrow(() -> new EntityNotFoundException("지역 점수 정보를 찾을 수 없습니다."));
        regionScore.setScore(regionScore.getScore() + score);

        // 4-3. 사용자 월별 기여도 업데이트 (없으면 새로 생성)
        int currentMonth = LocalDate.now().getYear() * 100 + LocalDate.now().getMonthValue();
        UserScoreMonth userScoreMonth = userScoreMonthRepository
                .findByUserIdAndSidoAndGugunAndMonth(userId, user.getSido(), user.getGugun(), currentMonth)
                .orElseGet(() -> UserScoreMonth.builder()
                        .user(user)
                        .sido(user.getSido())
                        .gugun(user.getGugun())
                        .month(currentMonth)
                        .score(0) // 초기 점수는 0
                        .build());
        userScoreMonth.setScore(userScoreMonth.getScore() + score);
        userScoreMonthRepository.save(userScoreMonth);

        // 4-4. 미션 인증 성공 기록 (Recommend 객체 자체를 넘겨줌)
        MissionAuthentication auth = MissionAuthentication.builder()
                .user(user)
                .mission(mission)
                .recommend(matchedRecommend) // recommend 객체를 통째로 저장
                .visitedAt(ocrData.getVisitedAt())
                .build();
        missionAuthenticationRepository.save(auth);

        return "미션 성공: " + score + "점을 획득했습니다.";
    }

    private boolean isMissionConditionSatisfied(Long missionId, OcrDataDTO ocrData) {
        // 기본 방문 미션의 경우, 장소 일치만으로 성공이므로 별도 로직이 필요 없을 수 있음
        // if (missionId == BASIC_VISIT_MISSION_ID) return true;

        // 8월 여름 휴가 미션 (ID: 1)
        if (missionId.equals(SUMMER_VACATION_MISSION_ID)) {
            if (ocrData.getVisitedAt() == null) return false;

            LocalDate visitedDate = ocrData.getVisitedAt().toLocalDate();
            LocalDate startDate = LocalDate.of(2025, 8, 22);
            LocalDate endDate = LocalDate.of(2025, 8, 27);

            // 방문일(visitedDate)이 시작일(startDate)과 같거나 이후이고,
            // 종료일(endDate)과 같거나 이전인지 확인
            return !visitedDate.isBefore(startDate) && !visitedDate.isAfter(endDate);
        }

        // AI 가게 추천 (ID: 2) 은 장소 일치만으로 통과되므로 별도 조건 없음

        // 점심 시간 방문 미션 (ID: 3)
        if (missionId.equals(LUNCH_VISIT_MISSION_ID)) {
            if (ocrData.getVisitedAt() == null) return false;
            int hour = ocrData.getVisitedAt().getHour();
            return hour >= 12 && hour < 14;
        }

        // 15,000원 이상 소비 미션 (ID: 4)
        if (missionId.equals(SPEND_15000_MISSION_ID)) {
            return ocrData.getTotalPrice() != null && ocrData.getTotalPrice() >= 15000;
        }

        // 20,000원 이상 소비 미션 (ID: 5)
        if (missionId.equals(SPEND_20000_MISSION_ID)) {
            return ocrData.getTotalPrice() != null && ocrData.getTotalPrice() >= 20000;
        }

        // AI 방문 미션(ID: 2)처럼 장소 인증만 필요한 미션의 경우 true를 반환
        return true;
    }

    //미션 메인페이지
    public MissionResponseDTO.MissionRsDTO missionMain(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // 지역 점수 테이블에 해당 지역 있는 지 확인 > 없으면 생성해 둠
        regionScoreRepository.findBySidoAndGugun(user.getSido(), user.getGugun())
                .orElseGet(() -> {
                    RegionScore regionScore = RegionScore.builder()
                            .sido(user.getSido())
                            .gugun(user.getGugun())
                            .build();
                    return regionScoreRepository.save(regionScore);
                });

        //같은 시도 내 region, me rank 계산
        //region
        List<RegionScore> allRegionScores = regionScoreRepository.findAllBySidoOrderByScoreDesc(user.getSido());

        int regionRankVal = 1;
        int regionScoreVal = 0;
        for (int i = 0; i < allRegionScores.size(); i++) {
            RegionScore rs = allRegionScores.get(i);
            if (rs.getGugun().equals(user.getGugun())) {
                regionRankVal = i + 1;
                regionScoreVal = rs.getScore();
                break;
            }
        }
        //me
        List<User> allUsers = userRepository.findAllBySidoOrderByTotalScoreDesc(user.getSido());

        int meRankVal = 1;
        int meScoreVal = user.getTotalScore();
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getId().equals(user.getId())) {
                meRankVal = i + 1;
                break;
            }
        }

        // 해커톤 시에는 기간에 따라 변화하는 것을 보여주지 못하기 때문에 현재는 전체 미션 리스트를 반환하나
        // 확장 시, 미션 기간을 정해 현재 기간에 맞춰 진행 가능한 미션만 내려 보낼 수 있도록 해야함
        List<MissionResponseDTO.MissionRsDTO.MissionDTO> missions = missionRepository.findAll()
                .stream()
                .map(mission -> {
                    // 인증 기록 가져오기
                    MissionAuthentication auth = missionAuthenticationRepository.findByUserAndMission(user, mission)
                            .orElse(null);

                    boolean isVerified = (auth != null);
                    boolean isReviewed = false;

                    if (auth != null) {
                        // review 테이블에서 mission_authentication_id 로 체크
                        isReviewed = reviewRepository.existsByMissionAuthentication(auth);
                    }

                    return MissionResponseDTO.MissionRsDTO.MissionDTO.builder()
                            .id(mission.getId())
                            .title(mission.getTitle())
                            .body(mission.getBody())
                            .score(mission.getScore())
                            .is_special(mission.isSpecial())
                            .is_verified(isVerified)
                            .is_reviewed(isReviewed)
                            .build();
                })
                .toList();



        return MissionResponseDTO.MissionRsDTO.builder()
                .gugun(user.getGugun())
                .region( // 지역 rank/score
                        MissionResponseDTO.MissionRsDTO.RankScoreDTO.builder()
                                .rank(regionRankVal)
                                .score(regionScoreVal)
                                .build()
                )
                .me( // 내 rank/score
                        MissionResponseDTO.MissionRsDTO.RankScoreDTO.builder()
                                .rank(meRankVal)
                                .score(meScoreVal)
                                .build()
                )
                .missions(missions)
                .build();
    }
}

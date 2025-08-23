package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.recommendDTO.OcrDataDTO;
import com.flexswu.flexswu.entity.*;
import com.flexswu.flexswu.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
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
    public String authenticateMission(Long userId, Long missionId, OcrDataDTO ocrData) {
        // 1. 필수 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new EntityNotFoundException("미션을 찾을 수 없습니다. ID: " + missionId));

        // 사용자의 가장 최근 추천 장소(Recommend) 조회
        Recommend latestRecommend = recommendRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalStateException("추천받은 장소 기록이 없습니다."));

        // 2. 장소 일치 여부 확인 (도로명 주소 우선, 다음으로 지번, 없으면 전화번호로)
        // OCR의 'address'는 Recommend의 'roadAddress'와 비교
        boolean isPlaceMatch = Objects.equals(ocrData.getAddress(), latestRecommend.getRoadAddress()) ||
                Objects.equals(ocrData.getAddress(), latestRecommend.getAddress()) ||
                Objects.equals(ocrData.getPhoneNum(), latestRecommend.getPhoneNum());

        if (!isPlaceMatch) {
            throw new IllegalArgumentException("추천 장소와 영수증의 장소가 일치하지 않습니다.");
        }

        // 3. 미션별 세부 조건 확인
        if (!isMissionConditionSatisfied(missionId, ocrData)) {
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
                .recommend(latestRecommend) // recommend 객체를 통째로 저장
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
}

package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.recommendDTO.RecommendMainResponseDTO;
import com.flexswu.flexswu.dto.recommendDTO.RecommendRequestDTO;
import com.flexswu.flexswu.dto.recommendDTO.RecommendResponseDTO;
import com.flexswu.flexswu.dto.userDTO.UserPreferenceUpdateDTO;
import com.flexswu.flexswu.entity.Recommend;
import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.repository.RecommendRepository;
import com.flexswu.flexswu.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {
    private final UserRepository userRepository;
    private final RecommendRepository recommendRepository;
    private final FastApiService fastApiService;
    private final UserPreferenceService userPreferenceService;

    //추천 받기 (조회용)
    public RecommendResponseDTO.RecommendFullResponseDTO recommendToday(RecommendRequestDTO.RecommendRqDTO request, Long userId, Boolean weather) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        //오늘 추천 받았으면 에러
        //최근 추천 기록 조회
        Recommend latest = recommendRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);

        //최근 추천이 있고, 자정이 지나면 초기화
        if (latest != null && latest.getCreatedAt().toLocalDate().isEqual(LocalDate.now())) {
            throw new IllegalArgumentException("오늘 이미 추천을 받았습니다.");
        }

        if(!request.getRegion().contains(user.getGugun())){
            throw new IllegalArgumentException("본인 지역 x");
        }

        // HTTP 요청 DTO -> FastAPI 요청 DTO 변환
        RecommendRequestDTO.RecommendFastDTO fastBody = toFastApiBody(request, user, weather);

        // fastAPI 호출해서 결과 전체 받음
        RecommendResponseDTO.RecommendListDTO response = fastApiService.getRecommendations(fastBody, weather);

        return RecommendResponseDTO.RecommendFullResponseDTO.builder()
                .place_mood(request.getPlace_mood())
                .category(response.getWeather() != null ? response.getWeather() : request.getPlace_category())
                .stores(response.getRecommendations())
                .build();
    }

    /**
     * HTTP 요청 DTO -> FastAPI DTO 변환
     */
    private RecommendRequestDTO.RecommendFastDTO toFastApiBody(
            RecommendRequestDTO.RecommendRqDTO rq, User user, Boolean weather
    ) {
        String regionJoin = String.join(" ", rq.getRegion()); //ex 노원구 공릉동
        String searchQuery = user.getSido() + " " + regionJoin; //ex 서울 노원구 공릉동

        String convertedCategory = null;

        //기본 추천일 때만 > 장소 카테고리 카카오 맵 키워드 검색 기준으로 변환
        if (weather == null || !weather) {
            convertedCategory = convertCategory(rq.getPlace_category());
        }

        List<RecommendRequestDTO.PreviousPlaceDTO> previousPlaces = null;

        //중복 방지용 장소 리스트 생성
        if (!rq.getDuplicate()) {
            Recommend latest = recommendRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);
            //직전 추천 존재 시 && 직전 추천 카테고리와 카테고리가 동일할 시에
            if (latest != null && latest.getCategory().equals(rq.getPlace_category())) {
                LocalDateTime latestTime = latest.getCreatedAt(); //추천 생성 시간으로 추천 리스트들 불러옴
                List<Recommend> latestRecommends = recommendRepository.findAllByUserAndCreatedAt(user, latestTime);

                previousPlaces = latestRecommends.stream()
                        .map(r -> RecommendRequestDTO.PreviousPlaceDTO.builder()
                                .name(r.getName())
                                .address(r.getRoadAddress())
                                .build())
                        .toList();
            }
        }

        return RecommendRequestDTO.RecommendFastDTO.builder()
                .mood_keywords(rq.getPlace_mood())
                .place_category(convertedCategory)
                .search_query(searchQuery)
                .previous_places(previousPlaces)
                .build();
    }

    /**
     * FastAPI 응답 -> http 응답 DTO 변환
     */
    private RecommendResponseDTO.RecommendFastDTO toRsDTO(RecommendResponseDTO.RecommendFastDTO fast) {
        return RecommendResponseDTO.RecommendFastDTO.builder()
                .name(fast.getName())
                .addressRoad(fast.getAddressRoad())
                .addressEx(fast.getAddressEx())
                .phone(fast.getPhone())
                .url(fast.getUrl())
                .build();
    }

    //추천 받기 (최종저장용)
    @Transactional
    public int finalSave(RecommendRequestDTO.RecommendRqFinalSaveDTO request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        LocalDateTime now = LocalDateTime.now();

        List<Recommend> toSave = request.getStores().stream()
                .map(store -> Recommend.builder()
                        .user(user)
                        .name(store.getName())
                        .category(request.getCategory())
                        .roadAddress(store.getAddress_road())
                        .address(store.getAddress_ex())
                        .url(store.getUrl())
                        .phoneNum(store.getPhone())
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .toList();

        // 1. 추천된 장소 목록은 항상 DB에 저장
        recommendRepository.saveAll(toSave);

        int surveyCount = 0; // 설문 횟수 변수 선언

        // 2. '분위기 키워드(place_mood)'가 요청에 포함된 경우에만 설문 횟수 업데이트 로직을 수행
        if (request.getPlace_mood() != null && !request.getPlace_mood().isEmpty()) {

            UserPreferenceUpdateDTO preferenceUpdateDTO = new UserPreferenceUpdateDTO();
            preferenceUpdateDTO.setSelectedCategories(request.getPlace_mood());

            // 2-1. userPreferenceService를 호출하여 설문 횟수 업데이트
            surveyCount = userPreferenceService.updateUserPreference(userId, preferenceUpdateDTO);

            // 2-2. 설문 횟수가 10 이상이면 사용자 유형 분석 로직 호출
            if (surveyCount >= 10) {
                userPreferenceService.getUserType(userId);
            }
        }

        // 3. 설문 횟수를 컨트롤러로 반환
        return surveyCount;
    }


    //카테고리 매핑
    private String convertCategory(String category) {
        return switch (category) {
            case "한식당" -> "한식";
            case "일식당" -> "일식";
            case "중식당" -> "중식";
            case "양식집" -> "양식";
            case "분식집" -> "분식";
            case "커피 전문점" -> "카페";
            case "호프집" -> "술집";
            case "일본식 주점" -> "이자카야";
            case "제과점, 베이커리" -> "빵";
            case "아이스크림 가게" -> "아이스크림";
            case "소품샵" -> "디자인문구";
            default -> throw new IllegalArgumentException("정의되지 않은 카테고리");
        };
    }

    // 추천 메인 페이지 조회
    public RecommendMainResponseDTO getRecommendMainPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // 오늘 날짜의 시작 시간 (오늘 추천/과거 추천을 나누는 기준)
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        // 1. 오늘 추천받은 기록 조회
        List<Recommend> todayRecommends = recommendRepository.findAllByUserAndCreatedAtAfter(user, startOfToday);
        List<RecommendMainResponseDTO.StoreDTO> todayStores = todayRecommends.stream()
                .map(this::toStoreDTO)
                .distinct() // 중복 제거 로직 추가
                .collect(Collectors.toList());

        // 2. 과거 추천 기록 조회
        List<RecommendMainResponseDTO.StoreDTO> pastStores = new ArrayList<>(); // 결과를 담을 리스트 초기화

        // 2-1. 가장 최근의 '과거 추천 기록' 한 개를 먼저 찾아서, 추천받은 시각(timestamp)을 알아냄
        Optional<Recommend> latestPastRec = recommendRepository.findTopByUserAndCreatedAtBeforeOrderByCreatedAtDesc(user, startOfToday);

        // 2-2. 만약 과거 추천 기록이 존재한다면,
        if (latestPastRec.isPresent()) {
            // 그 기록의 정확한 생성 시각을 가져옴
            LocalDateTime latestPastTimestamp = latestPastRec.get().getCreatedAt();

            // 2-3. 해당 시각에 저장된 '모든' 추천 기록들을 전부 불러옴 (한 묶음 전체)
            List<Recommend> allLatestPastRecommends = recommendRepository.findAllByUserAndCreatedAt(user, latestPastTimestamp);

            // 2-4. 불러온 기록들을 DTO로 변환
            pastStores = allLatestPastRecommends.stream()
                    .map(this::toStoreDTO)
                    .distinct() // 중복 제거 로직 추가
                    .collect(Collectors.toList());
        }

        // 3. 응답 DTO 조립
        return RecommendMainResponseDTO.builder()
                .username(user.getUsername()) // User 엔티티의 사용자 이름 필드로 가정
                .gugun(user.getGugun())
                .todayRecommend(RecommendMainResponseDTO.TodayRecommendDTO.builder().stores(todayStores).build())
                .pastRecommend(pastStores)
                .build();
    }

    // Recommend 엔티티를 StoreDTO로 변환하는 헬퍼 메소드
    private RecommendMainResponseDTO.StoreDTO toStoreDTO(Recommend recommend) {
        return RecommendMainResponseDTO.StoreDTO.builder()
                .name(recommend.getName())
                .category(recommend.getCategory())
                .address(recommend.getRoadAddress()) // 대표 주소로 도로명 주소 사용
                .url(recommend.getUrl())
                .build();
    }

}

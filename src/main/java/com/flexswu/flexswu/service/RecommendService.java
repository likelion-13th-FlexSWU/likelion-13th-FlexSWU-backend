package com.flexswu.flexswu.service;

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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendService {
    private final UserRepository userRepository;
    private final RecommendRepository recommendRepository;
    private final FastApiService fastApiService;
    private final UserPreferenceService userPreferenceService;

    //추천 받기 (조회용)
    public RecommendResponseDTO.RecommendFullResponseDTO recommendToday(RecommendRequestDTO.RecommendRqDTO request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        //오늘 추천 받았으면 에러
        //최근 추천 기록 조회
        Recommend latest = recommendRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);

        //최근 추천이 있고, 생성 시간이 24시간 이내면 에러
        if (latest != null && latest.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24))) {
            throw new IllegalArgumentException("오늘 이미 추천을 받았습니다.");
        }

        if(!request.getRegion().contains(user.getGugun())){
            throw new IllegalArgumentException("본인 지역 x");
        }

        // HTTP 요청 DTO -> FastAPI 요청 DTO 변환
        RecommendRequestDTO.RecommendFastDTO fastBody = toFastApiBody(request, user);

        // fastAPI 호출해서 추천 리스트 받아옴
        List<RecommendResponseDTO.RecommendFastDTO> stores = fastApiService.getRecommendations(fastBody);

        return RecommendResponseDTO.RecommendFullResponseDTO.builder()
                .place_mood(request.getPlace_mood())
                .category(request.getPlace_category())
                .stores(stores)
                .build();
    }

    /**
     * HTTP 요청 DTO -> FastAPI DTO 변환
     */
    private RecommendRequestDTO.RecommendFastDTO toFastApiBody(
            RecommendRequestDTO.RecommendRqDTO rq, User user
    ) {
        String regionJoin = String.join(" ", rq.getRegion()); //ex 노원구 공릉동
        String searchQuery = user.getSido() + " " + regionJoin; //ex 서울 노원구 공릉동

        return RecommendRequestDTO.RecommendFastDTO.builder()
                .mood_keywords(rq.getPlace_mood())
                .place_category(rq.getPlace_category())
                .search_query(searchQuery)
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

        //base entity 사용 X > 수동 지정 (장소들 시각 통일을 위한)
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

        recommendRepository.saveAll(toSave);

        UserPreferenceUpdateDTO preferenceUpdateDTO = new UserPreferenceUpdateDTO();
        preferenceUpdateDTO.setSelectedCategories(request.getPlace_mood());

        // 1. userPreferenceService를 호출하고, 반환된 설문 횟수를 변수에 저장
        int surveyCount = userPreferenceService.updateUserPreference(userId, preferenceUpdateDTO);

        // 2. 설문 횟수가 10 이상인지 확인
        if (surveyCount >= 10) {
            // 3. 10 이상이면 사용자 유형 분석 로직을 호출
            userPreferenceService.getUserType(userId);
        }

        // 4. 설문 횟수를 컨트롤러로 반환
        return surveyCount;
    }
}

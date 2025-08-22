package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.recommendDTO.RecommendRequestDTO;
import com.flexswu.flexswu.dto.recommendDTO.RecommendResponseDTO;
import com.flexswu.flexswu.entity.Recommend;
import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.repository.RecommendRepository;
import com.flexswu.flexswu.repository.UserRepository;
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

        //장소 카테고리 카카오 맵 키워드 검색 기준으로 변환
        String convertedCategory = convertCategory(rq.getPlace_category());

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
    public void finalSave(RecommendRequestDTO.RecommendRqFinalSaveDTO request, Long userId) {
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
}

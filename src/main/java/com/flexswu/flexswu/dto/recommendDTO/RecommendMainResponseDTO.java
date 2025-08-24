package com.flexswu.flexswu.dto.recommendDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendMainResponseDTO {

    private String username;
    private String gugun;

    @JsonProperty("today_recommend")
    private TodayRecommendDTO todayRecommend;

    @JsonProperty("past_recommend")
    private List<StoreDTO> pastRecommend;

    @Getter
    @Builder
    public static class TodayRecommendDTO {
        private List<StoreDTO> stores;
    }

    // 오늘/과거 추천 가게 정보 DTO (공통)
    @Getter
    @Builder
    @EqualsAndHashCode(of = "url")
    public static class StoreDTO {
        private String name;
        private String category;
        private String address;
        private String url;
    }
}
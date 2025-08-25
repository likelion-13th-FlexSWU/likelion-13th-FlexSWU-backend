package com.flexswu.flexswu.dto.recommendDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class RecommendResponseDTO {
    // === 추천 받기 조회용 ===
    //최종 http 프론트 응답용 dto
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendFullResponseDTO {
        private List<String> place_mood;
        private String category;
        private List<RecommendFastDTO> stores;
    }

    //fast api 응답용 dto
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendFastDTO {
        private String name;
        @JsonProperty("address_road")
        private String addressRoad;
        @JsonProperty("address_ex")
        private String addressEx;
        private String phone;
        private String url;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendListDTO {
        private String weather;
        private List<RecommendFastDTO> recommendations;
    }
}

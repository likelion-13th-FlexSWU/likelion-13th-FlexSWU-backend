package com.flexswu.flexswu.dto.recommendDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class RecommendResponseDTO {
    //추천 받기 응답
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendRsDTO{
        private String category;
        private String name;
        private String address;
        private String phone_num;
        private String url;
    }

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
        private List<RecommendFastDTO> recommendations;
    }

}

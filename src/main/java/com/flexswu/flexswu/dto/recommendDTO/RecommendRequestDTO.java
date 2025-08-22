package com.flexswu.flexswu.dto.recommendDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class RecommendRequestDTO {
    //추천 받기 http 요청용 (조회용)
    @Getter
    @Builder
    public static class RecommendRqDTO{
        @NotEmpty
        private List<String> region;

        @NotBlank
        private String place_category;

        @NotEmpty
        private List<String> place_mood;

        @NotNull
        private Boolean duplicate;
    }

    //추천 받기 fastapi용
    @Getter
    @Builder
    public static class RecommendFastDTO{
        private List<String> mood_keywords;
        private String place_category;
        private String search_query;
    }

    //추천 받기 (최종 저장용)
    @Getter
    @Builder
    public static class RecommendRqFinalSaveDTO {
        @NotEmpty
        private List<String> place_mood;

        @NotBlank
        private String category;

        @NotEmpty
        private List<RecommendStoreDTO> stores;

        @Getter
        @Builder
        public static class RecommendStoreDTO {
            @NotBlank
            private String name;

            @NotBlank
            private String address_road;

            private String address_ex;

            private String phone;

            @NotBlank
            private String url;
        }
    }

}

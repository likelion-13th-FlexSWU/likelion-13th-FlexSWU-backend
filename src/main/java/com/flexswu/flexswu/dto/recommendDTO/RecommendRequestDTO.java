package com.flexswu.flexswu.dto.recommendDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class RecommendRequestDTO {
    //추천 받기 http요청용
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
}

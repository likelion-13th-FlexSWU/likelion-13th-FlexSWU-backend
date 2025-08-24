package com.flexswu.flexswu.dto.missionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.*;
import java.util.List;

public class MissionResponseDTO {

    // 미션 메인페이지 - 최상위 응답
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissionRsDTO {
        private String gugun;
        private RankScoreDTO region;
        private RankScoreDTO me;
        private List<MissionDTO> missions;

        // region / me 랭킹, 스코어
        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RankScoreDTO {
            private int rank;
            private int score;
        }

        // 미션 구조
        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MissionDTO {
            private Long id;
            private String title;
            private String body;
            private int score;
            private boolean is_special;   // JSON: is_special
            private boolean is_verified;  // JSON: is_verified
            private boolean is_reviewed;   // JSON: is_reviewd
        }
    }
}

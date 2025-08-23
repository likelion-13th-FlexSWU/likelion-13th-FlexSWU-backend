package com.flexswu.flexswu.dto.userDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserResponseDTO {
    //로그인 응답
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResultRsDTO{
        private String access_token;
        private String refresh_token;
        private Long user_id;
    }

    // 마이페이지
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoRsDTO {
        private String sido;
        private String gugun;
        private String username;
        private String type;
        private List<MonthlyScoreDTO> monthly;

        //사용자 월별 기여도 점수
        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MonthlyScoreDTO {
            private String month;
            private int score;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessTokenRsDTO {
        private String access_token;
    }

}

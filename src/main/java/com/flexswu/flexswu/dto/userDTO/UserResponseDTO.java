package com.flexswu.flexswu.dto.userDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessTokenRsDTO{
        private String access_token;
    }

}

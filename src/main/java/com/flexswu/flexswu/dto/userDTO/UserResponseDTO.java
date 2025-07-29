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
        String access_token;
        String refresh_token;
        Long user_id;
    }

}

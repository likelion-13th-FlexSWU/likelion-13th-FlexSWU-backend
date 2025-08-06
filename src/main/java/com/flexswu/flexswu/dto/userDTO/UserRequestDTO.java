package com.flexswu.flexswu.dto.userDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class UserRequestDTO {
    //회원가입
    @Getter
    public static class CreateUserRqDTO{
        @NotNull
        @NotBlank
        @Size(max = 12)
        String identify;

        @NotNull
        @NotBlank
        @Size(max = 12)
        String password;

        @NotNull
        @NotBlank
        @Size(max = 15)
        String username;

        Boolean marketingAgree;
    }
    //로그인
    @Getter
    public static class LoginRqDTO{
        @NotNull
        @NotBlank
        @Size(max = 12)
        String identify;

        @NotNull
        @NotBlank
        @Size(max = 12)
        String password;
    }


}

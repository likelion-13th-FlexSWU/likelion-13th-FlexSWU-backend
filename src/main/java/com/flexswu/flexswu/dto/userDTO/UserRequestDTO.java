package com.flexswu.flexswu.dto.userDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class UserRequestDTO {
    //회원가입
    @Getter
    public static class CreateUserRqDTO{
        @NotBlank
        @Size(max = 12)
        String identify;

        @NotBlank
        @Size(max = 12)
        String password;

        @NotBlank
        @Size(max = 15)
        String username;

        @NotBlank
        @Size(max = 15)
        String sido;

        @NotBlank
        @Size(max = 15)
        String gugun;

        Boolean marketing_agree;
    }
    //로그인
    @Getter
    public static class LoginRqDTO{
        @NotBlank
        @Size(max = 12)
        String identify;

        @NotBlank
        @Size(max = 12)
        String password;
    }

    //아이디 중복 확인
    @Getter
    public static class checkRqDTO {
        @NotBlank
        @Size(max = 12)
        String identify;
    }

    //지역 변경
    @Getter
    public static class regionRqDTO {
        @NotBlank
        @Size(max = 15)
        String sido;

        @NotBlank
        @Size(max = 15)
        String gugun;
    }

    //닉네임 변경
    @Getter
    public static class usernameRqDTO {
        @NotBlank
        @Size(max = 15)
        String username;
    }

}

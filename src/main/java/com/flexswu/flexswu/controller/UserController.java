package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.dto.userDTO.UserRequestDTO;
import com.flexswu.flexswu.dto.userDTO.UserResponseDTO;
import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.jwt.CustomUserDetails;
import com.flexswu.flexswu.jwt.TokenStatus;
import com.flexswu.flexswu.repository.UserRepository;
import com.flexswu.flexswu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(
            @RequestBody @Valid UserRequestDTO.CreateUserRqDTO request) {
        userService.createUser(request);
        return ResponseEntity.ok("회원가입 완료");
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO.UserResultRsDTO> login(
            @RequestBody @Valid UserRequestDTO.LoginRqDTO request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    //액세스 토큰 재발급
    @GetMapping("/refresh")
    public ResponseEntity<UserResponseDTO.AccessTokenRsDTO> refreshAccessToken(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        // refresh Token 유효성 검증
        if (userService.validateRefreshToken(refreshToken) != TokenStatus.AUTHENTICATED) {
            return ResponseEntity.status(401).body(new UserResponseDTO.AccessTokenRsDTO("유효하지 않은 리프레시 토큰입니다."));
        }

        // 토큰에서 id 추출
        String identify = userService.extractUsernameFromRefresh(refreshToken);

        // 새 Access Token 발급
        String newAccessToken = userService.reissueAccessToken(identify, refreshToken);

        return ResponseEntity.ok(new UserResponseDTO.AccessTokenRsDTO(newAccessToken));
    }

    //아이디 중복 확인
    @PostMapping("/check")
    public ResponseEntity<Boolean> checkId(
            @RequestBody @Valid UserRequestDTO.checkRqDTO request) {
        boolean isCheck = userRepository.findByIdentify(request.getIdentify()).isPresent();
        return ResponseEntity.ok(isCheck);
    }

    //지역 변경
    @PatchMapping("/update/region")
    public ResponseEntity<String> updateRegion(
            @RequestBody @Valid UserRequestDTO.regionRqDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(userService.updateRegion(request, userDetails.getUserId()));
    }
}

package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.dto.userDTO.UserClusterResponseDTO;
import com.flexswu.flexswu.dto.userDTO.UserPreferenceUpdateDTO;
import com.flexswu.flexswu.dto.userDTO.UserRequestDTO;
import com.flexswu.flexswu.dto.userDTO.UserResponseDTO;
import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.jwt.CustomUserDetails;
import com.flexswu.flexswu.jwt.TokenStatus;
import com.flexswu.flexswu.repository.UserRepository;
import com.flexswu.flexswu.service.UserPreferenceService;
import com.flexswu.flexswu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserPreferenceService userPreferenceService;

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

    // 사용자가 선택한 가게 분위기 카테고리를 받아 누적 카운트 업데이트
    @PostMapping("/preferences")
    public ResponseEntity<Integer> updateUserPreferences(Principal principal,
                                                         @RequestBody UserPreferenceUpdateDTO updateDto) {
        String identify = principal.getName();
        User user = userRepository.findByIdentify(identify)
                .orElseThrow(() -> new RuntimeException("User not found with identify: " + identify));
        Long userId = user.getId();

        int surveyCount = userPreferenceService.updateUserPreference(userId, updateDto);
        return ResponseEntity.ok(surveyCount);
    }

    // 사용자 유형(타입)을 AI 모델을 통해 분석하여 반환
    @GetMapping("/type")
    public ResponseEntity<UserClusterResponseDTO> getMyType(Principal principal) {
        String identify = principal.getName();
        User user = userRepository.findByIdentify(identify)
                .orElseThrow(() -> new RuntimeException("User not found with identify: " + identify));
        Long userId = user.getId();

        UserClusterResponseDTO userType = userPreferenceService.getUserType(userId);
        return ResponseEntity.ok(userType);
    }

    // 전체 사용자 데이터를 기반으로 AI 모델 재학습 요청
    @PostMapping("/admin/train-model")
    public ResponseEntity<String> trainModel() {
        try {
            userPreferenceService.trainAiModel();
            return ResponseEntity.ok("AI model training has been successfully requested.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("모델링 시작 실패: " + e.getMessage());
        }
    }

    //지역 변경
    @PatchMapping("/update/region")
    public ResponseEntity<String> updateRegion(
            @RequestBody @Valid UserRequestDTO.regionRqDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(userService.updateRegion(request, userDetails.getUserId()));
    }

    //닉네임 변경
    @PatchMapping("/update/nick")
    public ResponseEntity<String> updateUsername(
            @RequestBody @Valid UserRequestDTO.usernameRqDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(userService.updateUsername(request, userDetails.getUserId()));
    }

    //내 정보
    @GetMapping("")
    public ResponseEntity<UserResponseDTO.UserInfoRsDTO> userInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(userService.userInfo(userDetails.getUserId()));
    }
}

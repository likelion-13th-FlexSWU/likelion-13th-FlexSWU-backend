package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.userDTO.UserRequestDTO;
import com.flexswu.flexswu.dto.userDTO.UserResponseDTO;
import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.jwt.JwtUtil;
import com.flexswu.flexswu.jwt.TokenStatus;
import com.flexswu.flexswu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public void createUser(UserRequestDTO.CreateUserRqDTO request){
        // 기존 사용자 확인
        if (userRepository.findByIdentify(request.getIdentify()).isPresent()) {
            throw new RuntimeException("이미 존재하는 사용자입니다.");
        }

        // 새로운 사용자 생성
        User user = User.builder()
                .identify(request.getIdentify())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .marketingAgree(request.getMarketingAgree())
                .build();

        userRepository.save(user);

    }

    public UserResponseDTO.UserResultRsDTO loginUser(UserRequestDTO.LoginRqDTO request){
        // 사용자 조회
        User user = userRepository.findByIdentify(request.getIdentify())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 해싱된 비밀번호 비교
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // refresh 토큰 저장
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return UserResponseDTO.UserResultRsDTO.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken)
                .user_id(user.getId())
                .build();
    }

    //액세스 토큰 재발급
    public TokenStatus validateRefreshToken(String token) {
        return jwtUtil.validateRefreshToken(token);
    }

    public String extractUsernameFromRefresh(String token) {
        return jwtUtil.extractIdentifyFromRefresh(token);
    }

    public String reissueAccessToken(String identify, String requestRefreshToken) {
        User user = userRepository.findByIdentify(identify)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        if (!user.getRefreshToken().equals(requestRefreshToken)) {
            throw new RuntimeException("리프레시 토큰이 일치하지 않습니다.");
        }

        return jwtUtil.generateAccessToken(user);
    }

}

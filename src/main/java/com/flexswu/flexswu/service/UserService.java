package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.userDTO.UserRequestDTO;
import com.flexswu.flexswu.dto.userDTO.UserResponseDTO;
import com.flexswu.flexswu.entity.RegionScore;
import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.jwt.JwtUtil;
import com.flexswu.flexswu.jwt.TokenStatus;
import com.flexswu.flexswu.repository.RegionScoreRepository;
import com.flexswu.flexswu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RegionScoreRepository regionScoreRepository;
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
                .sido(request.getSido())
                .gugun(request.getGugun())
                .regionUpdated(LocalDate.now())
                .marketingAgree(request.getMarketing_agree())
                .build();

        userRepository.save(user);

        // 지역 점수 테이블에 해당 지역 있는 지 확인 > 없으면 생성해 둠
        regionScoreRepository.findBySidoAndGugun(request.getSido(), request.getGugun())
                .orElseGet(() -> {
                    RegionScore regionScore = RegionScore.builder()
                            .sido(request.getSido())
                            .gugun(request.getGugun())
                            .build();
                    return regionScoreRepository.save(regionScore);
                });

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

    //지역 변경
    public String updateRegion(UserRequestDTO.regionRqDTO request, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        LocalDate lastUpdated = user.getRegionUpdated();
        LocalDate now = LocalDate.now();

        //마지막 변경일이 있고 + 2개월이 지나지 않았으면 예외
        if (lastUpdated.plusMonths(2).isAfter(now)) {
            throw new IllegalArgumentException("지역 변경은 마지막 변경일로부터 2개월 후에 가능합니다.");
        }

        if(request.getSido().equals(user.getSido()) && request.getGugun().equals(user.getGugun())){
            throw new IllegalArgumentException("현재 사용 중인 지역과 동일합니다.");
        }

        user.setSido(request.getSido());
        user.setGugun(request.getGugun());
        user.setRegionUpdated(now);
        userRepository.save(user);

        // 지역 점수 테이블에 해당 지역 있는 지 확인 > 없으면 생성해 둠
        regionScoreRepository.findBySidoAndGugun(request.getSido(), request.getGugun())
                .orElseGet(() -> {
                    RegionScore regionScore = RegionScore.builder()
                            .sido(request.getSido())
                            .gugun(request.getGugun())
                            .build();
                    return regionScoreRepository.save(regionScore);
                });

        return "지역 변경 완료";
    }

    //닉네임 변경
    public String updateUsername(UserRequestDTO.usernameRqDTO request, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        if(user.getUsername().equals(request.getUsername())){
            throw new IllegalArgumentException("현재 사용 중인 닉네임과 동일합니다.");
        }

        user.setUsername(request.getUsername());
        userRepository.save(user);

        return "닉네임 변경 완료";
    }

}

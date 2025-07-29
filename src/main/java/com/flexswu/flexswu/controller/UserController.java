package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.dto.userDTO.UserRequestDTO;
import com.flexswu.flexswu.dto.userDTO.UserResponseDTO;
import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.repository.UserRepository;
import com.flexswu.flexswu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    //회원가입
    @PostMapping("/user/signup")
    public ResponseEntity<String> signup(
            @RequestBody @Valid UserRequestDTO.CreateUserRqDTO request) {
        userService.createUser(request);
        return ResponseEntity.ok("회원가입 완료");
    }

    //로그인
    @PostMapping("/user/login")
    public ResponseEntity<UserResponseDTO.UserResultRsDTO> login(
            @RequestBody @Valid UserRequestDTO.LoginRqDTO request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }
}

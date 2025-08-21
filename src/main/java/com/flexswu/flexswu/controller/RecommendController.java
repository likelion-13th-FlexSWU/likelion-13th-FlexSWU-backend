package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.dto.userDTO.UserRequestDTO;
import com.flexswu.flexswu.repository.UserRepository;
import com.flexswu.flexswu.service.RecommendService;
import com.flexswu.flexswu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendController {
    //private final  userRepository;
    private final RecommendService recommendService;

    //추천 받기
    @PostMapping("/today")
    public ResponseEntity<?> recommendToday(
            @RequestBody @Valid UserRequestDTO.CreateUserRqDTO request) {
        return ResponseEntity.ok("");
    }
}

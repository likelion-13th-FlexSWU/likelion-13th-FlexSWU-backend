package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.dto.recommendDTO.RecommendRequestDTO;
import com.flexswu.flexswu.dto.recommendDTO.RecommendResponseDTO;
import com.flexswu.flexswu.dto.userDTO.UserRequestDTO;
import com.flexswu.flexswu.jwt.CustomUserDetails;
import com.flexswu.flexswu.repository.UserRepository;
import com.flexswu.flexswu.service.RecommendService;
import com.flexswu.flexswu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendController {
    private final RecommendService recommendService;

    //추천 받기 (조회용)
    @PostMapping("/today")
    public ResponseEntity<RecommendResponseDTO.RecommendFullResponseDTO> recommendToday(
            @RequestBody @Valid RecommendRequestDTO.RecommendRqDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(recommendService.recommendToday(request, userDetails.getUserId()));
    }

    //추천 받기 (최종 저장용)
    @PostMapping("/save")
    public ResponseEntity<Void> recommendSave(
            @RequestBody @Valid RecommendRequestDTO.RecommendRqFinalSaveDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        recommendService.finalSave(request, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

}

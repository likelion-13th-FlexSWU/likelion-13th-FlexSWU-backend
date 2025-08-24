package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.dto.missionDTO.MissionResponseDTO;
import com.flexswu.flexswu.dto.reviewDTO.OcrDataDTO;
import com.flexswu.flexswu.jwt.CustomUserDetails;
import com.flexswu.flexswu.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mission")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @PostMapping("/check")
    public ResponseEntity<String> authenticate(
            @RequestBody OcrDataDTO ocrData,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();

        try {
            String resultMessage = missionService.authenticateMission(currentUserId, ocrData);
            return ResponseEntity.ok(resultMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //미션 메인페이지
    @GetMapping("")
    public ResponseEntity<MissionResponseDTO.MissionRsDTO> getAllMissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(missionService.missionMain(userDetails.getUserId()));
    }
}

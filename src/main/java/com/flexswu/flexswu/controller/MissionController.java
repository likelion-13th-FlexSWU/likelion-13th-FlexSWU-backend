package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.dto.reviewDTO.OcrDataDTO;
import com.flexswu.flexswu.jwt.CustomUserDetails;
import com.flexswu.flexswu.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @PostMapping("/mission/check")
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
}

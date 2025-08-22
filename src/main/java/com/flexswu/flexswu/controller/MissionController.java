package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.dto.recommendDTO.OcrDataDTO;
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

    @PostMapping("/{missionId}/authenticate")
    public ResponseEntity<String> authenticate(
            @PathVariable Long missionId,
            @RequestBody OcrDataDTO ocrData,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = userDetails.getUserId();

        try {
            String resultMessage = missionService.authenticateMission(currentUserId, missionId, ocrData);
            return ResponseEntity.ok(resultMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.service.FastApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/external/past")
public class FastApiController {

    private final FastApiService pastApiService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        try {
            String body = pastApiService.pingRootText(); // 서비스 통해 호출
            return ResponseEntity.ok(body == null ? "" : body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Fast API 호출 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}

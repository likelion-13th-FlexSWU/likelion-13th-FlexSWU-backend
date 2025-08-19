package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.service.FastApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/external/fast")
public class FastApiController {

    private final FastApiService fastApiService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        try {
            String body = fastApiService.pingRootText(); // 서비스 통해 호출
            return ResponseEntity.ok(body == null ? "" : body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Fast API 호출 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @GetMapping("/echo")
    public ResponseEntity<String> echo(@RequestParam String msg) {
        try {
            String body = fastApiService.echo(msg);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Fast API 호출 실패: " + e.getMessage());
        }
    }
}

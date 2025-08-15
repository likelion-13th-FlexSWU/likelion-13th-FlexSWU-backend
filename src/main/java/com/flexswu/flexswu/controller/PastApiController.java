package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.service.PastApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/external/past")
public class PastApiController {

    private final PastApiService pastApiService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return pastApiService.pingRoot();
    }
}

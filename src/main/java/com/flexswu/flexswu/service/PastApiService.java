package com.flexswu.flexswu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class PastApiService {

    private final RestClient pastApiRestClient;

    public ResponseEntity<String> pingRoot() {
        return pastApiRestClient.get()
                .uri("/")   // 환경변수에 설정한 base-url + "/"
                .retrieve()
                .toEntity(String.class);
    }
}
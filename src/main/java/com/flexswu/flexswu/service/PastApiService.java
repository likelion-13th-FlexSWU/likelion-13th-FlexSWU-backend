package com.flexswu.flexswu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class PastApiService {

    private final RestClient pastApiRestClient;

    public String pingRootText() {
        return pastApiRestClient.get()
                .uri("/")                          // 외부 Past API의 루트
                .accept(MediaType.APPLICATION_JSON) // 루트가 JSON이면 OK, text여도 String으로 수신 가능
                .retrieve()
                .body(String.class);
    }
}

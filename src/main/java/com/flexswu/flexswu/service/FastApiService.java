package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.recommendDTO.RecommendRequestDTO;
import com.flexswu.flexswu.dto.recommendDTO.RecommendResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FastApiService {

    private final RestClient fastApiRestClient;

    // test
    public String pingRootText() {
        return fastApiRestClient.get()
                .uri("/")                          // 외부 Past API의 루트
                .accept(MediaType.APPLICATION_JSON) // 루트가 JSON이면 OK, text여도 String으로 수신 가능
                .retrieve()
                .body(String.class);
    }
    // test
    public String echo(String msg) {
        return fastApiRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/")
                        .queryParam("msg", msg)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    // 추천 받기 api 호출
    public List<RecommendResponseDTO.RecommendFastDTO> getRecommendations(
            RecommendRequestDTO.RecommendFastDTO body
    ) {
        RecommendResponseDTO.RecommendListDTO wrapper = fastApiRestClient.post()
                .uri("/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(RecommendResponseDTO.RecommendListDTO.class);

        return wrapper != null ? wrapper.getRecommendations() : List.of();
    }


}

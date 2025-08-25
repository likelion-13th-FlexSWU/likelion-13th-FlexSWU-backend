package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.userDTO.UserBehaviorDataDTO;
import com.flexswu.flexswu.dto.userDTO.UserClusterResponseDTO;
import com.flexswu.flexswu.dto.recommendDTO.RecommendRequestDTO;
import com.flexswu.flexswu.dto.recommendDTO.RecommendResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FastApiService {

    private final RestClient fastApiRestClient;

    @Value("${fast.api.base-url}")
    private String fastapiUrl;

    // test
    public String pingRootText() {
        return fastApiRestClient.get()
                .uri("/")
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

    // FastAPI 서버로 사용자 행동 데이터 보내고 클러스터(유형) 결과를 받음
    public UserClusterResponseDTO requestUserCluster(UserBehaviorDataDTO userBehaviorData) {
        String url = fastapiUrl + "/user-cluster";

        return fastApiRestClient.post() // 1. POST 요청 시작
                .uri(url) // 2. URI 설정
                .contentType(MediaType.APPLICATION_JSON) // 3. Body가 JSON 타입임을 명시
                .body(userBehaviorData) // 4. 전송할 Body 데이터 설정
                .retrieve() // 5. 요청 실행 및 응답 수신
                .body(UserClusterResponseDTO.class); // 6. 응답 Body를 DTO로 변환
    }

    // FastAPI 서버로 전체 사용자 데이터 보내 모델 재학습 요청
    public void requestModelTraining(List<UserBehaviorDataDTO> allUsersData) {
        String url = fastapiUrl + "/model/train";

        fastApiRestClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(allUsersData)
                .retrieve()
                .toBodilessEntity(); // 7. 응답 Body가 없거나 무시할 경우
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

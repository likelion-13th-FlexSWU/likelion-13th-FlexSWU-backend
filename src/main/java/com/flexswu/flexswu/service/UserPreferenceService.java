// UserBehaviorService.java
package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.userDTO.UserBehaviorDataDTO;
import com.flexswu.flexswu.dto.userDTO.UserClusterResponseDTO;
import com.flexswu.flexswu.dto.userDTO.UserPreferenceUpdateDTO;
import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.entity.UserPreference;
import com.flexswu.flexswu.repository.UserPreferenceRepository;
import com.flexswu.flexswu.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final FastApiService fastApiService;
    private final EntityManager em; // Reflection을 사용한 동적 업데이트를 위해 사용
    private final UserRepository userRepository;

    private static final Map<String, String> KOREAN_TO_ENGLISH_MAP = new HashMap<>();

    static {
        KOREAN_TO_ENGLISH_MAP.put("혼밥 하기 편해요", "soloFriendly");
        KOREAN_TO_ENGLISH_MAP.put("데이트하기 좋아요", "dateFriendly");
        KOREAN_TO_ENGLISH_MAP.put("가족과 가기 좋아요", "familyFriendly");
        KOREAN_TO_ENGLISH_MAP.put("메뉴가 다양해요", "diverseMenu");
        KOREAN_TO_ENGLISH_MAP.put("음악 선정이 좋아요", "goodMusic");
        KOREAN_TO_ENGLISH_MAP.put("책 읽기 좋아요", "bookFriendly");
        KOREAN_TO_ENGLISH_MAP.put("사진찍기 좋아요", "photoFriendly");
        KOREAN_TO_ENGLISH_MAP.put("활기찬 공간이에요", "lively");
        KOREAN_TO_ENGLISH_MAP.put("반려동물과 함께", "petFriendly");
        KOREAN_TO_ENGLISH_MAP.put("조용해요", "quiet");
        KOREAN_TO_ENGLISH_MAP.put("해외같아요", "exotic");
        KOREAN_TO_ENGLISH_MAP.put("집중하기 좋아요", "focus");
        KOREAN_TO_ENGLISH_MAP.put("뷰가 좋아요", "goodView");
        KOREAN_TO_ENGLISH_MAP.put("매장이 넓어요", "spacious");
        KOREAN_TO_ENGLISH_MAP.put("식물이 많아요", "plants");
        KOREAN_TO_ENGLISH_MAP.put("오래 머물기 좋아요", "longStay");
        KOREAN_TO_ENGLISH_MAP.put("트렌디해요", "trendy");
        KOREAN_TO_ENGLISH_MAP.put("시끌벅적해요", "noisy");
        KOREAN_TO_ENGLISH_MAP.put("인테리어가 감성적이에요", "aesthetic");
        KOREAN_TO_ENGLISH_MAP.put("아늑해요", "cozy");
    }


    // 사용자의 설문 횟수를 DB에 누적
    @Transactional
    public int updateUserPreference(Long userId, UserPreferenceUpdateDTO updateDto) {
        for (String koreanCategory : updateDto.getSelectedCategories()) {
            if (!KOREAN_TO_ENGLISH_MAP.containsKey(koreanCategory)) {
                // 유효하지 않은 카테고리가 발견되면 예외를 발생시켜 작업을 중단시킴
                throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + koreanCategory);
            }
        }

        UserPreference preference = userPreferenceRepository.findById(userId)
                .orElseGet(() -> UserPreference.builder().userId(userId).build());

        preference.incrementSurveyCount();

        for (String koreanCategory : updateDto.getSelectedCategories()) {
            // 1. 매핑 테이블에서 한글 카테고리에 해당하는 영문 필드명을 가져옴
            String englishFieldName = KOREAN_TO_ENGLISH_MAP.get(koreanCategory);

            // 2. 만약 매핑되는 필드가 없으면 건너뜀
            if (englishFieldName == null) {
                System.err.println("Invalid or unmapped category name: " + koreanCategory);
                continue;
            }

            // 3. 영문 필드명으로 필드를 찾아 값을 업데이트
            try {
                Field field = UserPreference.class.getDeclaredField(englishFieldName);
                field.setAccessible(true);
                int currentValue = (int) field.get(preference);
                field.set(preference, currentValue + 1);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // 이 오류는 이제 KOREAN_TO_ENGLISH_MAP에 필드명이 잘못된 경우에만 발생함
                System.err.println("Reflection error for field: " + englishFieldName);
            }
        }
        UserPreference savedPreference = userPreferenceRepository.save(preference);
        return savedPreference.getSurveyCount();
    }

    // 특정 사용자의 유형(클러스터)을 FastAPI를 통해 분석하여 가져온 후 user 테이블에 저장
    @Transactional
    public UserClusterResponseDTO getUserType(Long userId) {
        UserPreference preference = userPreferenceRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 1. 설문 횟수를 확인
        if (preference.getSurveyCount() < 10) {
            // 2. 10번 미만이면, FastAPI를 호출하지 않고 'None'을 담은 응답을 직접 만들어 반환
            return UserClusterResponseDTO.builder()
                    .cluster("None")
                    .userId(userId)
                    .build();
        }

        UserBehaviorDataDTO dto = UserBehaviorDataDTO.fromEntity(preference);
        UserClusterResponseDTO clusterResponse = fastApiService.requestUserCluster(dto);

        String receivedUserType = clusterResponse.getCluster();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.updateUserType(receivedUserType);

        return clusterResponse;
    }

    // DB에 저장된 모든 사용자 데이터를 FastAPI로 보내 모델을 재학습시킴
    public void trainAiModel() {
        List<UserPreference> allPreferences = userPreferenceRepository.findAll();
        List<UserBehaviorDataDTO> allUsersData = allPreferences.stream()
                .map(UserBehaviorDataDTO::fromEntity)
                .collect(Collectors.toList());

        if (allUsersData.isEmpty()) {
            throw new RuntimeException("모델링 할 사용자가 존재하지 않음.");
        }

        fastApiService.requestModelTraining(allUsersData);
    }
}
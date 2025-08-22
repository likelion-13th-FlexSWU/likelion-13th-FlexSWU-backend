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

    // 사용자가 선택한 분위기 카테고리 횟수를 DB에 누적
    @Transactional
    public int updateUserPreference(Long userId, UserPreferenceUpdateDTO updateDto) {
        UserPreference preference = userPreferenceRepository.findById(userId)
                .orElseGet(() -> UserPreference.builder().userId(userId).build());

        preference.incrementSurveyCount();

        for (String category : updateDto.getSelectedCategories()) {
            try {
                Field field = UserPreference.class.getDeclaredField(category);
                field.setAccessible(true);
                int currentValue = (int) field.get(preference);
                field.set(preference, currentValue + 1);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Invalid category name: " + category);
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
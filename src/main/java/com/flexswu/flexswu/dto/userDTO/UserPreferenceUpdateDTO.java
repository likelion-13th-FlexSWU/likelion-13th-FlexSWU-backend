package com.flexswu.flexswu.dto.userDTO;

import lombok.Getter;

import java.util.List;

@Getter
public class UserPreferenceUpdateDTO {
    private List<String> selectedCategories; // 예: ["quiet", "cozy"]
}

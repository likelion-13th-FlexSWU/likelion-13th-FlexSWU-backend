package com.flexswu.flexswu.dto.userDTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserPreferenceUpdateDTO {
    private List<String> selectedCategories; // 예: ["quiet", "cozy"]
}

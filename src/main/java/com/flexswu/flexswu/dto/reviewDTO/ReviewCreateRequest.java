package com.flexswu.flexswu.dto.reviewDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReviewCreateRequest {
    @NotNull(message = "mission_id는 필수입니다.")
    private Long mission_id;

    // 반드시 1~4개 선택 (UI 정책 반영)
    @NotNull(message = "tags는 필수입니다.")
    @Size(min = 1, max = 4, message = "tags는 1~4개를 선택해야 합니다.")
    private List<Integer> tags;

    // nullable 허용
    private String content;
}
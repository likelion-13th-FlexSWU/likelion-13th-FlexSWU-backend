package com.flexswu.flexswu.dto.reviewDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ReviewListResponse {
    private List<ReviewItemDTO> content;
}

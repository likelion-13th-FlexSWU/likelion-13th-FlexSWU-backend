package com.flexswu.flexswu.dto.reviewDTO;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class ReviewItemDTO {
    private String reviewid;
    private String placeName;
    private String title;
    private String content;
    private List<TagCodeDTO> tags;
    private String visitedAt;            // "yyyy-MM-dd"
    private OffsetDateTime createdAt;    // ISO-8601(+09:00)
}

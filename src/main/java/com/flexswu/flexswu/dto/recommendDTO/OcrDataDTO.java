package com.flexswu.flexswu.dto.recommendDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OcrDataDTO {
    @JsonProperty("mission_id")
    private Long missionId;

    private String address;

    private String name;

    @JsonProperty("visited_at")
    private LocalDateTime visitedAt;

    @JsonProperty("phone_num")
    private String phoneNum;

    @JsonProperty("total_price")
    private Integer totalPrice;
}

package com.flexswu.flexswu.dto.userDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserClusterResponseDTO {
    @JsonProperty("user_id")
    private Long userId;
    private String cluster;
}

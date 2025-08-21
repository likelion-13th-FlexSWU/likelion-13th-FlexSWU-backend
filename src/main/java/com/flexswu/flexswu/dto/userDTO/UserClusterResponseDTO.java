package com.flexswu.flexswu.dto.userDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserClusterResponseDTO {
    @JsonProperty("user_id")
    private Long userId;
    private String cluster;
}

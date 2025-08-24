package com.flexswu.flexswu.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RegionScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String sido;

    @Column(nullable = false, length = 15)
    private String gugun;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int score = 0;
}

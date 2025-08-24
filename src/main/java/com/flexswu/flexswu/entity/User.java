package com.flexswu.flexswu.entity;

import com.flexswu.flexswu.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String identify;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 15)
    private String username;

    private String refreshToken;

    private Boolean marketingAgree;

    @Column(nullable = false, length = 15)
    private String sido;

    @Column(nullable = false, length = 15)
    private String gugun;

    @Column(nullable = false)
    private LocalDate regionUpdated;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int totalScore = 0;

    @Column(nullable = true, length = 15)
    private String userType;

    public void updateUserType(String userType) {
        this.userType = userType;
    }
}

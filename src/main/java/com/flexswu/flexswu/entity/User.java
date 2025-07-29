package com.flexswu.flexswu.entity;

import com.flexswu.flexswu.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    //length 등은 기획 나오면 추가예정
    @Column(nullable = false, unique = true, length = 10)
    private String identify;

    @Column(nullable = false, length = 100)
    private String password;

    private String username;
    private String refreshToken;
}

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
public class Recommend extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 15)
    private String category;

    @Column(nullable = false, length = 100)
    private String roadAddress;

    @Column(length = 100)
    private String address;

    @Column(nullable = false, length = 150)
    private String url;

    @Column(length = 30)
    private String phoneNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

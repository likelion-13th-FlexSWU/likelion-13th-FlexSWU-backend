package com.flexswu.flexswu.entity;

import com.flexswu.flexswu.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_authentication_id")
    private MissionAuthentication missionAuthentication;

    private String placeName;

    // 내용 (nullable 허용)
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime visitedAt;

    // 태그 코드 리스트([1,2] 형태)
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "code")
    private List<Integer> tagCodes = new ArrayList<>();
}
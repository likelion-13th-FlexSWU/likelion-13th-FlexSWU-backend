package com.flexswu.flexswu.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    private Long userId;

    @Builder.Default // 빌더로 객체 생성 시 기본값을 0으로 설정
    private int surveyCount = 0;

    // FastAPI의 UserBehaviorData 필드명과 유사하게 설정
    private int familyFriendly;
    private int dateFriendly;
    private int petFriendly;
    private int soloFriendly;
    private int quiet;
    private int cozy;
    private int focus;
    private int noisy;
    private int lively;
    private int diverseMenu;
    private int bookFriendly;
    private int plants;
    private int trendy;
    private int photoFriendly;
    private int goodView;
    private int spacious;
    private int aesthetic;
    private int longStay;
    private int goodMusic;
    private int exotic;

    public void incrementSurveyCount() {
        this.surveyCount++;
    }
}

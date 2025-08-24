package com.flexswu.flexswu.dto.userDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flexswu.flexswu.entity.UserPreference;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserBehaviorDataDTO {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("survey_count")
    private int surveyCount;

    @JsonProperty("family_friendly")
    private int familyFriendly;
    @JsonProperty("date_friendly")
    private int dateFriendly;
    @JsonProperty("pet_friendly")
    private int petFriendly;
    @JsonProperty("solo_friendly")
    private int soloFriendly;
    @JsonProperty("quiet")
    private int quiet;
    @JsonProperty("cozy")
    private int cozy;
    @JsonProperty("focus")
    private int focus;
    @JsonProperty("noisy")
    private int noisy;
    @JsonProperty("lively")
    private int lively;
    @JsonProperty("diverse_menu")
    private int diverseMenu;
    @JsonProperty("book_friendly")
    private int bookFriendly;
    @JsonProperty("plants")
    private int plants;
    @JsonProperty("trendy")
    private int trendy;
    @JsonProperty("photo_friendly")
    private int photoFriendly;
    @JsonProperty("good_view")
    private int goodView;
    @JsonProperty("spacious")
    private int spacious;
    @JsonProperty("aesthetic")
    private int aesthetic;
    @JsonProperty("long_stay")
    private int longStay;
    @JsonProperty("good_music")
    private int goodMusic;
    @JsonProperty("exotic")
    private int exotic;


    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static UserBehaviorDataDTO fromEntity(UserPreference entity) {
        return UserBehaviorDataDTO.builder()
                .userId(entity.getUserId())
                .surveyCount(entity.getSurveyCount())
                .familyFriendly(entity.getFamilyFriendly())
                .dateFriendly(entity.getDateFriendly())
                .petFriendly(entity.getPetFriendly())
                .soloFriendly(entity.getSoloFriendly())
                .quiet(entity.getQuiet())
                .cozy(entity.getCozy())
                .focus(entity.getFocus())
                .noisy(entity.getNoisy())
                .lively(entity.getLively())
                .diverseMenu(entity.getDiverseMenu())
                .bookFriendly(entity.getBookFriendly())
                .plants(entity.getPlants())
                .trendy(entity.getTrendy())
                .photoFriendly(entity.getPhotoFriendly())
                .goodView(entity.getGoodView())
                .spacious(entity.getSpacious())
                .aesthetic(entity.getAesthetic())
                .longStay(entity.getLongStay())
                .goodMusic(entity.getGoodMusic())
                .exotic(entity.getExotic())
                .build();
    }
}
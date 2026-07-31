package com.boardwise.backend.marketplace.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Genres {
    ABSTRACT("abstract"),
    ABSTRACT_STRATEGY("abstract strategy"),
    ACTION_DEXTERITY("action / dexterity"),
    ADVENTURE("adventure"),
    AGE_OF_REASON("age of reason"),
    AMERICAN_CIVIL_WAR("american civil war"),
    AMERICAN_INDIAN_WARS("american indian wars"),
    AMERICAN_REVOLUTIONARY_WAR("american revolutionary war"),
    AMERICAN_WEST("american west"),
    ANCIENT("ancient"),
    ANIMALS("animals"),
    ARABIAN("arabian"),
    AVIATION_FLIGHT("aviation / flight"),
    BLUFFING("bluffing"),
    BOOK("book"),
    CARD("card"),
    CARD_GAME("card game"),
    CHILDRENS_GAME("children's game"),
    CITY_BUILDING("city building"),
    CIVIL_WAR("civil war"),
    CIVILIZATION("civilization"),
    COLLECTIBLE_COMPONENTS("collectible components"),
    COMIC_BOOK_STRIP("comic book / strip"),
    DEDUCTION("deduction"),
    DICE("dice"),
    ECONOMIC("economic"),
    EDUCATIONAL("educational"),
    ELECTRONIC("electronic"),
    ENVIRONMENTAL("environmental"),
    EXPANSION_FOR_BASE_GAME("expansion for base-game"),
    EXPLORATION("exploration"),
    FAN_EXPANSION("fan expansion"),
    FANTASY("fantasy"),
    FARMING("farming"),
    FIGHTING("fighting"),
    GAME_SYSTEM("game system"),
    HORROR("horror"),
    HUMOR("humor"),
    INDUSTRY_MANUFACTURING("industry / manufacturing"),
    KOREAN_WAR("korean war"),
    MAFIA("mafia"),
    MATH("math"),
    MATURE_ADULT("mature / adult"),
    MAZE("maze"),
    MEDICAL("medical"),
    MEDIEVAL("medieval"),
    MEMORY("memory"),
    MINIATURES("miniatures"),
    MODERN_WARFARE("modern warfare"),
    MOVIES_TV_RADIO("movies / tv / radio theme"),
    MURDER_MYSTERY("murder / mystery"),
    MUSIC("music"),
    MYTHOLOGY("mythology"),
    NAPOLEONIC("napoleonic"),
    FAMILY("family"),
    NAUTICAL("nautical"),
    NEGOTIATION("negotiation"),
    NOVEL_BASED("novel-based"),
    NUMBER("number"),
    PARTY("party"),
    PARTY_GAME("party game"),
    PIKE_AND_SHOT("pike and shot"),
    PIRATES("pirates"),
    POLITICAL("political"),
    STRATEGY("strategy"),
    POST_NAPOLEONIC("post-napoleonic"),
    PREHISTORIC("prehistoric"),
    PRINT_AND_PLAY("print & play"),
    PUZZLE("puzzle"),
    RACING("racing"),
    REAL_TIME("real-time"),
    RELIGIOUS("religious"),
    RENAISSANCE("renaissance"),
    SCIENCE_FICTION("science fiction"),
    SPACE_EXPLORATION("space exploration"),
    SPIES_SECRET_AGENTS("spies / secret agents"),
    SPORTS("sports"),
    TERRITORY_BUILDING("territory building"),
    THIRD_PARTY_EXPANSION("third-party expansion"),
    TRAINS("trains"),
    TRANSPORTATION("transportation"),
    TRAVEL("travel"),
    TRIVIA("trivia"),
    VIDEO_GAME_THEME("video game theme"),
    VIETNAM_WAR("vietnam war"),
    WARGAME("wargame"),
    WORD_GAME("word game"),
    WORLD_WAR_I("world war i"),
    WORLD_WAR_II("world war ii"),
    ZOMBIES("zombies");

    private final String value;

    Genres(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Genres fromValue(String value) {
        for (Genres genre : values()) {
            if (genre.value.equalsIgnoreCase(value)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Unknown genre value: " + value);
    }
}

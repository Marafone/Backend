package com.marafone.marafone.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SelectCardResult {
    SUCCESS("Card played successfully"),
    ERROR("An error occurred"),
    NOT_YOUR_TURN("It is not your turn"),
    GAME_ENDED("The game has ended");

    private final String message;
}

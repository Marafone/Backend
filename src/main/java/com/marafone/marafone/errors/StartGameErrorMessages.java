package com.marafone.marafone.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StartGameErrorMessages {
    OWNER_MISMATCH("You are not the owner of this game."),
    GAME_ALREADY_STARTED("The game has already started."),
    TEAMS_NOT_FULL("Not all teams are full. Please ensure all teams are complete.");

    private final String message;
}

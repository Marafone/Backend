package com.marafone.marafone.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JoinGameResult {
    SUCCESS("You successfully joined the game"),
    GAME_NOT_FOUND("Selected game does not exist"),
    TEAMS_FULL("Game team is already full"),
    GAME_ALREADY_STARTED("The game has already started"),
    INCORRECT_PASSWORD("Provided password was incorrect"),
    PLAYER_ALREADY_JOINED("You have already joined this game");

    private final String message;
}

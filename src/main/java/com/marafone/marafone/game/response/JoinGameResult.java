package com.marafone.marafone.game.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JoinGameResult {
    SUCCESS("You successfully joined the game"),
    GAME_NOT_FOUND("The selected game does not exist. Please try refreshing the page."),
    TEAMS_FULL("Game team is already full"),
    GAME_ALREADY_STARTED("The game has already started"),
    INCORRECT_PASSWORD("Provided password was incorrect"),
    PLAYER_ALREADY_JOINED("You have already joined this game"),
    PLAYER_DURING_ANOTHER_GAME("You are already a member of another game");

    private final String message;
}

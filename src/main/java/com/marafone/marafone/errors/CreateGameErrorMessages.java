package com.marafone.marafone.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreateGameErrorMessages {
    GAME_NAME_TAKEN("Game name already taken. Try with the other name."),
    PLAYER_LEFT_ANOTHER_GAME("You are already a member of another game that you left. " +
            "Either rejoin the game you left or wait until that game ends.");

    private final String message;
}

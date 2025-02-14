package com.marafone.marafone.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MakeAIMoveResult {
    SUCCESS("AI move applied successfully"),
    GAME_NOT_FOUND("The specified game does not exist"),
    NO_AI_FOUND("No AI found for the given player"),
    NO_VALID_MOVES("No valid moves available for AI"),
    MOVE_FAILED("Failed to apply AI move");

    private final String message;
}
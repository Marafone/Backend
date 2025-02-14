package com.marafone.marafone.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AddAIResult {
    SUCCESS("AI player added successfully"),
    MAX_AI_REACHED("Maximum number of AI players reached"),
    GAME_NOT_FOUND("The specified game does not exist"),
    FAILED_TO_ADD("Failed to add AI player"),
    AI_LOAD_ERROR("Failed to load AI model");

    private final String message;
}

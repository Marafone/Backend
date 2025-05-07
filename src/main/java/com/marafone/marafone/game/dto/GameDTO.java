package com.marafone.marafone.game.dto;

import com.marafone.marafone.game.model.GameType;

public record GameDTO(String gameId, String gameName, GameType gameType, Integer joinedPlayersAmount, boolean isPrivate) {
}

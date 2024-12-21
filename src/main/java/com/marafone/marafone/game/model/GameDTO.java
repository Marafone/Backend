package com.marafone.marafone.game.model;


public record GameDTO(Long gameId, String gameName, GameType gameType, Integer joinedPlayersAmount) {
}

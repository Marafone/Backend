package com.marafone.marafone.game.model;


public record GameDTO(String gameId, String gameName, GameType gameType, Integer joinedPlayersAmount) {
}

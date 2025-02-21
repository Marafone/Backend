package com.marafone.marafone.game.model;

public record CurrentGameContext(Long gameId, GamePlayer currentPlayer, Round currentRound, Card selectedCard) {
}

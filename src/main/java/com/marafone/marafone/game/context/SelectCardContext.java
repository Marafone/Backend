package com.marafone.marafone.game.context;

import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.model.Round;

public record SelectCardContext(Long gameId, GamePlayer currentPlayer, Round currentRound, Card selectedCard) {
}

package com.marafone.marafone.game.random.cards;

import com.marafone.marafone.game.model.GamePlayer;

import java.util.List;
public interface RandomCardsAssigner {
    void assignRandomCardsToPlayers(List<GamePlayer> gamePlayers);
}

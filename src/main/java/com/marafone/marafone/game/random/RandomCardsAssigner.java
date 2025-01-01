package com.marafone.marafone.game.random;

import com.marafone.marafone.game.model.GamePlayer;

import java.util.List;
public interface RandomCardsAssigner {
    void AssignRandomCardsToPlayers(List<GamePlayer> gamePlayers);
}

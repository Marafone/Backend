package com.marafone.marafone.game.random.order;

import com.marafone.marafone.game.model.GamePlayer;

import java.util.List;
public interface RandomInitialOrderAssigner {
    List<GamePlayer> assignRandomInitialOrder(List<GamePlayer> gamePlayers);
}

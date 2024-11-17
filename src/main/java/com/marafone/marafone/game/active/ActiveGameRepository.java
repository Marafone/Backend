package com.marafone.marafone.game.active;

import com.marafone.marafone.game.logic.GameLogic;

import java.util.Optional;

public interface ActiveGameRepository {
    Optional<GameLogic> findById(Long id);
    Long put(GameLogic gameLogic);
}

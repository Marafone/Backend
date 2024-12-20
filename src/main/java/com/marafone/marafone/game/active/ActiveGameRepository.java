package com.marafone.marafone.game.active;

import com.marafone.marafone.game.model.Game;

import java.util.List;
import java.util.Optional;

public interface ActiveGameRepository {
    Optional<Game> findById(Long id);
    Long put(Game game);
    List<Game> getWaitingGames();
}

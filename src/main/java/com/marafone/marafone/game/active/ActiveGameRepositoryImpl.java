package com.marafone.marafone.game.active;

import com.marafone.marafone.game.model.Game;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveGameRepositoryImpl implements ActiveGameRepository{

    ConcurrentHashMap<Long, Game> activeGames = new ConcurrentHashMap<>();

    @Override
    public Optional<Game> findById(Long id) {
        return null;
    }

    /* Generate random int, put it into hashmap and return id*/
    @Override
    public Long put(Game game) {
        return null;
    }
}

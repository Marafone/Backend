package com.marafone.marafone.game.active;

import com.marafone.marafone.game.logic.GameLogic;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveGameRepositoryImpl implements ActiveGameRepository{

    ConcurrentHashMap<Long, GameLogic> activeGames = new ConcurrentHashMap<>();

    @Override
    public Optional<GameLogic> findById(Long id) {
        return Optional.ofNullable(activeGames.get(id));
    }

    /* Generate random int, put it into hashmap and return id*/
    @Override
    public Long put(GameLogic gameLogic) {
        return null;
    }
}

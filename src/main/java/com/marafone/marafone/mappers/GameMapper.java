package com.marafone.marafone.mappers;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.GameDTO;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    public GameDTO toGameDTO(Game game) {
        return new GameDTO(game.getId(), game.getName(), game.getGameType(), game.getPlayersAmount());
    }
}

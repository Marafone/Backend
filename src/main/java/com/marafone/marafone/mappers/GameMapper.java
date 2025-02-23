package com.marafone.marafone.mappers;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.dto.GameDTO;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    public GameDTO toGameDTO(Game game) {
        return new GameDTO(String.valueOf(game.getId()), game.getName(), game.getGameType(), game.getPlayersAmount());
    }
}

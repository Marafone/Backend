package com.marafone.marafone.game.event.incoming;

import com.marafone.marafone.game.model.GameType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateGameRequest {
    GameType gameType;
    String joinGameCode;
}

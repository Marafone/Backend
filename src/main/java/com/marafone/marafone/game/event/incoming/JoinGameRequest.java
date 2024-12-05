package com.marafone.marafone.game.event.incoming;

import com.marafone.marafone.game.model.Team;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JoinGameRequest {
    public Team team;
    public String joinGameString;
}

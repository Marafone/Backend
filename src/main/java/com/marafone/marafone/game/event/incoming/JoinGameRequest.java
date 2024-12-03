package com.marafone.marafone.game.event.incoming;

import com.marafone.marafone.game.model.Team;

public class JoinGameRequest {
    private Long gameId;
    private Team team;
    private String joinGameString;
}

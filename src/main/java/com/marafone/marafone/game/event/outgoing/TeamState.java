package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.Team;

import java.util.HashMap;
import java.util.Map;

public final class TeamState extends OutEvent{

    public Map<String, Team> teamState; //username -> team

    public TeamState(Game game) {
        super("TeamState");

        teamState = new HashMap<>();

        for(var gamePlayer: game.getPlayersList()){
            teamState.put(gamePlayer.getUser().getUsername(), gamePlayer.getTeam());
        }
    }
}

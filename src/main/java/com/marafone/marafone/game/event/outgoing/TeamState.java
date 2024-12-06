package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Team;

import java.util.Map;

public final class TeamState extends OutEvent{

    public Map<String, Team> teamState; //username -> team

    public TeamState(Map<String, Team> teamState) {
        super("TeamState");
        this.teamState = teamState;
    }
}

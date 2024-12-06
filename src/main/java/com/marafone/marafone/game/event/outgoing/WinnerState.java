package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Team;

public final class WinnerState extends OutEvent{

    public Team winnerTeam;

    public WinnerState(Team winnerTeam){
        super("WinnerState");
        this.winnerTeam = winnerTeam;
    }
}

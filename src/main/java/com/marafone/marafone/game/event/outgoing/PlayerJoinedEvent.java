package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Team;

public final class PlayerJoinedEvent extends OutEvent {

    public String playerName;
    public Team team;

    public PlayerJoinedEvent(String playerName, Team team) {
        super("PlayerJoinedEvent");
        this.playerName = playerName;
        this.team = team;
    }
}

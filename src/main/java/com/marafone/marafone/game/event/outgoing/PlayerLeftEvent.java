package com.marafone.marafone.game.event.outgoing;

public final class PlayerLeftEvent extends OutEvent {
    public String playerName;

    public PlayerLeftEvent(String playerName) {
        super("PlayerLeftEvent");
        this.playerName = playerName;
    }
}

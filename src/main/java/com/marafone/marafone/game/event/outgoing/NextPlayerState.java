package com.marafone.marafone.game.event.outgoing;

public final class NextPlayerState extends OutEvent {

    public String playerName;
    public boolean isFirstPlayer;

    public NextPlayerState(String playerName, boolean isFirstPlayer) {
        super("NextPlayerState");
        this.playerName = playerName;
        this.isFirstPlayer = isFirstPlayer;
    }
}

package com.marafone.marafone.game.event.outgoing;

public abstract sealed class OutEvent permits
        MyCardsState, NewRound, PlayersOrderState, PointState,
        TeamState, TrumpSuitState, TurnState, WinnerState,
        PlayerJoinedEvent, PlayerLeftEvent, GameStartedEvent,
        ErrorEvent, NextPlayerState, CallState {

    public final String eventType;

    OutEvent(String eventType){
        this.eventType = eventType;
    }
}

package com.marafone.marafone.game.event.outgoing;

public sealed abstract class OutEvent permits
        MyCardsState, NewRound, PlayersOrderState, PointState,
        TeamState, TrumpSuitState, TurnState, WinnerState,
        PlayerJoinedEvent, GameStartedEvent {

    public final String eventType;

    OutEvent(String eventType){
        this.eventType = eventType;
    }
}

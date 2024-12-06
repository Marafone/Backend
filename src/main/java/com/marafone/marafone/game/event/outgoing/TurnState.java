package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Card;

import java.util.Map;

public final class TurnState extends OutEvent{

    public Map<String, Card> turn; //username -> card

    public TurnState(Map<String, Card> turn){
        super("TurnState");
        this.turn = turn;
    }
}

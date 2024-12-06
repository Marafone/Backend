package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Suit;

public final class TrumpSuitState extends OutEvent{

    public Suit trumpSuit;

    public TrumpSuitState(Suit trumpSuit){
        super("TrumpSuitState");
        this.trumpSuit = trumpSuit;
    }
}

package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.Suit;

public final class TrumpSuitState extends OutEvent{

    public Suit trumpSuit;

    public TrumpSuitState(Game game){
        super("TrumpSuitState");

        trumpSuit = game.getRounds().getLast().getTrumpSuit();
    }
}

package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.Round;
import com.marafone.marafone.game.model.Suit;

public final class TrumpSuitState extends OutEvent{

    public Suit trumpSuit;

    public TrumpSuitState(Game game){
        super("TrumpSuitState");

        Round currentRound = game.getRounds().getLast();
        if(currentRound != null)
            trumpSuit = game.getRounds().getLast().getTrumpSuit();
        else
            trumpSuit = null;
    }
}

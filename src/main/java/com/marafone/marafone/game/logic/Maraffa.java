package com.marafone.marafone.game.logic;

import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;

public class Maraffa extends GameLogic{
    @Override
    public synchronized boolean selectCard(CardSelectEvent cardSelectEvent, String principalName) {
        return false;
    }
    @Override
    public synchronized boolean selectSuit(TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName) {
        return false;
    }
}

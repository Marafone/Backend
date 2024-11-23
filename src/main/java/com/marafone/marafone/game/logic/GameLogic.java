package com.marafone.marafone.game.logic;

import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.GamePlayer;

public abstract class GameLogic {
    Game game;

    /* Each method returns true if call to this method was legal and false otherwise */

    public synchronized boolean addPlayer(JoinGameRequest joinGameRequest, String principalName) {
        return false;
    }
    public synchronized boolean startGame(String principalName){
        return false;
    }
    public synchronized boolean checkTimeout(){
        return false;
    }
    public abstract boolean selectCard(CardSelectEvent cardSelectEvent, String principalName);
    public abstract boolean selectSuit(TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName);
}

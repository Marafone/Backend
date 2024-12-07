package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.GamePlayer;

import java.util.List;

public final class MyCardsState extends OutEvent{

    public List<Card> myCards;

    public MyCardsState(List<Card> myCards) {
        super("MyCardsState");
        this.myCards = myCards;
    }

    public MyCardsState(GamePlayer gamePlayer){
        super("MyCardsState");
        this.myCards = gamePlayer.getOwnedCards();
    }
}
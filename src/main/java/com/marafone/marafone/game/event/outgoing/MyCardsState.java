package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Card;
import java.util.List;

public final class MyCardsState extends OutEvent{

    public List<Card> myCards;

    public MyCardsState(List<Card> myCards) {
        super("MyCardsState");
        this.myCards = myCards;
    }
}
package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Card;
import lombok.Data;

import java.util.List;

@Data
public class MyCardsState extends OutEvent{
    private List<Card> myCards;
}

package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.model.Suit;

import java.util.List;
import java.util.Map;

public class GameState {
    private List<Card> myCards;
    private List<GamePlayer> playersOrder;
    private Suit trumpSuit;
    private Map<GamePlayer, Integer> turn;
}

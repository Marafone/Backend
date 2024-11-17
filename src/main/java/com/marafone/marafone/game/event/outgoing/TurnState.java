package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.GamePlayer;

import java.util.Map;

public class TurnState {
    Map<GamePlayer, Card> turn;
}

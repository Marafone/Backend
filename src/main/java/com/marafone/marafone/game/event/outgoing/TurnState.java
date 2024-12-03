package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Card;
import lombok.Data;

import java.util.Map;

@Data
public class TurnState extends OutEvent{
    Map<String, Card> turn; //username -> card
}

package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Suit;
import lombok.Data;


@Data
public class TrumpSuitState extends OutEvent{
    Suit trumpSuit;
}

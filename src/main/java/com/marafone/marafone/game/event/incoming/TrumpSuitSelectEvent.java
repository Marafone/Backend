package com.marafone.marafone.game.event.incoming;

import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.model.Suit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class TrumpSuitSelectEvent {
    public Suit trumpSuit;
}

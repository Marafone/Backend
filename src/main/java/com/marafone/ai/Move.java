package com.marafone.ai;

import com.marafone.marafone.game.model.*;

import java.util.ArrayList;
import java.util.List;

public class Move {

    Card card;
    Suit suit;

    public Move(Card selectedCard, Suit selectedSuit) {
        card = selectedCard;
        suit = selectedSuit;
    }

    public Card getCard() {
        return card;
    }

    public Suit getSuit() {
        return suit;
    }
}

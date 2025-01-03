package com.marafone.marafone.game.random;

import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.model.Suit;

import java.util.List;

/** Abstraction for logic in games that is based on randomness, for easier mocking in tests. */

public interface RandomAssigner {
    Suit getRandomTrumpSuit();
    void assignRandomCardsToPlayers(List<GamePlayer> gamePlayers);
    Card getRandomCorrectCard(List<Card> cards);
    Card getRandomCorrectCard(List<Card> cards, Suit trumpSuit);
    List<GamePlayer> assignRandomInitialOrder(List<GamePlayer> gamePlayers);
}

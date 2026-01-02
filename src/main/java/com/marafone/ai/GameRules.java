package com.marafone.ai;

import com.marafone.marafone.game.model.*;
import java.util.*;

public class GameRules {

    // Kopiujemy logikę walidacji ruchów z TrainingLoop/Service
    public static List<Move> getValidMoves(Game game, GamePlayer player) {
        List<Move> validMoves = new ArrayList<>();
        // Jeśli gra się skończyła lub nie ma rund
        if (game.getRounds().isEmpty()) return validMoves;

        Round currentRound = game.getRounds().getLast();
        Suit trumpSuit = currentRound.getTrumpSuit();
        Suit leadingSuit = game.getLeadingSuit();
        List<Card> hand = player.getOwnedCards();

        // 1. Wybór atu (jeśli trzeba)
        if (trumpSuit == null) {
            for (Suit suit : Suit.values()) {
                validMoves.add(new Move(null, suit));
            }
            return validMoves;
        }

        // 2. Zagrywanie karty
        if (leadingSuit == null) {
            // Wychodzimy pierwsi - każda karta jest ok
            for (Card card : hand) validMoves.add(new Move(card, null));
        } else {
            // Musimy grać pod kolor
            List<Card> matchingSuitCards = hand.stream()
                    .filter(card -> card.getSuit() == leadingSuit)
                    .toList();
            if (!matchingSuitCards.isEmpty()) {
                for (Card card : matchingSuitCards) validMoves.add(new Move(card, null));
            } else {
                // Nie mamy do koloru - każda karta jest ok
                for (Card card : hand) validMoves.add(new Move(card, null));
            }
        }
        return validMoves;
    }

    // Określa kto wygrał lewę (skopiowane i uproszczone z ActiveGameServiceImpl)
    public static GamePlayer getTrickWinner(List<Action> actions, Suit trumpSuit) {
        if (actions.isEmpty()) return null;

        Action winningAction = actions.get(0);
        Suit leadingSuit = winningAction.getCard().getSuit();

        for (int i = 1; i < actions.size(); i++) {
            Action challenger = actions.get(i);
            Card winCard = winningAction.getCard();
            Card chalCard = challenger.getCard();

            boolean challengerWins = false;

            if (chalCard.getSuit() == trumpSuit && winCard.getSuit() != trumpSuit) {
                challengerWins = true;
            } else if (chalCard.getSuit() == winCard.getSuit()) {
                // Ta sama barwa (atu albo wiodąca) - decyduje starszeństwo
                if (chalCard.getRank().compareTo(winCard.getRank()) > 0) { // Zakładam, że compareTo działa poprawnie wg logiki gry
                    challengerWins = true;
                }
            }

            if (challengerWins) {
                winningAction = challenger;
            }
        }
        return winningAction.getPlayer();
    }

    public static int calculatePoints(List<Action> actions) {
        return actions.stream().mapToInt(a -> a.getCard().getRank().getPoints()).sum();
    }
}
package com.marafone.ai;

import com.marafone.marafone.game.active.ActiveGameRepository;
import com.marafone.marafone.game.active.ActiveGameService;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserRepository;
import com.marafone.marafone.game.random.RandomAssigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import java.io.*;

@Component
public class TrainingLoop {

    private final ActiveGameService activeGameService;
    private final ActiveGameRepository activeGameRepository;
    private final UserRepository userRepository;

    @Autowired
    private RandomAssigner randomAssigner;

    @Autowired
    public TrainingLoop(ActiveGameService activeGameService, ActiveGameRepository activeGameRepository,
                        UserRepository userRepository) {
        this.activeGameService = activeGameService;
        this.activeGameRepository = activeGameRepository;
        this.userRepository = userRepository;
    }

    public static List<Move> getValidMoves(Game game, GamePlayer player) {
        List<Move> validMoves = new ArrayList<>();

        if (game.getRounds().isEmpty()) {
            return validMoves; // No valid moves if no round has started
        }

        Round currentRound = game.getRounds().getLast();
        Suit trumpSuit = currentRound.getTrumpSuit();
        Suit leadingSuit = game.getLeadingSuit();  // Leading suit for the trick
        List<Card> hand = player.getOwnedCards();

        if (trumpSuit == null) {
            // If leading the trick, AI has to choose the trump suit (if not already set)
            for (Suit suit : Suit.values()) {
                validMoves.add(new Move(null, suit)); // Choosing a trump suit
            }
        }
        else if (leadingSuit == null) {
            for (Card card : hand) {
                validMoves.add(new Move(card, null));
            }
        }
        else {
            // If not leading, must follow suit if possible
            List<Card> matchingSuitCards = hand.stream()
                    .filter(card -> card.getSuit().equals(leadingSuit))
                    .toList();
            if (!matchingSuitCards.isEmpty()) {
                for (Card card : matchingSuitCards) {
                    validMoves.add(new Move(card, null));
                }
            } else {
                // If no matching suit, can play any card
                for (Card card : hand) {
                    validMoves.add(new Move(card, null));
                }
            }
        }

        return validMoves;
    }
}
package com.marafone.ai;

import com.marafone.marafone.game.active.ActiveGameRepository;
import com.marafone.marafone.game.active.ActiveGameService;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserRepository;
import com.marafone.marafone.game.random.RandomAssigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java.io.*;

import static com.marafone.marafone.game.model.JoinGameResult.SUCCESS;

@Component
public class TrainingLoop {

    private final ActiveGameService activeGameService;
    private final ActiveGameRepository activeGameRepository;
    private final UserRepository userRepository;

    @Autowired
    private List<Card> allCards;

    @Autowired
    private RandomAssigner randomAssigner;

    @Autowired
    public TrainingLoop(ActiveGameService activeGameService, ActiveGameRepository activeGameRepository,
                        UserRepository userRepository) {
        this.activeGameService = activeGameService;
        this.activeGameRepository = activeGameRepository;
        this.userRepository = userRepository;
    }

    public void runTraining(int episodes) {
        // Create dummy users
        User userA = DummyData.getUserA();
        User userB = DummyData.getUserB();
        User userC = DummyData.getUserC();
        User userD = DummyData.getUserD();

        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);
        userRepository.save(userD);

        // Initialize the epsilon-greedy strategy and AI
        EpsilonGreedy epsilonGreedy = new EpsilonGreedy(1.0, 0.1, 0.99);
        MarafoneAI ai = new MarafoneAI(epsilonGreedy, "Qvalue");

        // Run the training loop for the specified number of episodes
        for (int episode = 0; episode < episodes; episode++) {
            // Create a new game
            CreateGameRequest createGameRequest = new CreateGameRequest("Training Game", GameType.MARAFFA, "ABC");
            Long gameId = activeGameService.createGame(createGameRequest, userA);

            // Join players to the game
            JoinGameRequest joinRedTeam = new JoinGameRequest(Team.RED, "ABC");
            JoinGameRequest joinBlueTeam = new JoinGameRequest(Team.BLUE, "ABC");

            activeGameService.joinGame(gameId, joinRedTeam, userB); // Teammate
            activeGameService.joinGame(gameId, joinBlueTeam, userC); // Enemy 1
            activeGameService.joinGame(gameId, joinBlueTeam, userD); // Enemy 2

            // Get the game and players
            Game game = activeGameRepository.findById(gameId).orElseThrow();
            GamePlayer playerA = game.getPlayersList().stream().filter(p -> p.getUser().equals(userA)).findFirst().orElseThrow();
            GamePlayer playerB = game.getPlayersList().stream().filter(p -> p.getUser().equals(userB)).findFirst().orElseThrow();
            GamePlayer playerC = game.getPlayersList().stream().filter(p -> p.getUser().equals(userC)).findFirst().orElseThrow();
            GamePlayer playerD = game.getPlayersList().stream().filter(p -> p.getUser().equals(userD)).findFirst().orElseThrow();

            // Mock card assignment
            randomAssigner.assignRandomCardsToPlayers(game.getPlayersList());

            // Start the game
            activeGameService.startGame(gameId, userA.getUsername());

            // Simulate the game
            while (!game.hasEnded()) {
                for (GamePlayer player : game.getPlayersList()) {
                    if (player.getUser().equals(userA) || player.getUser().equals(userB)) {
                        // AI players (userA and userB)
                        List<Move> validMoves = getValidMoves(game, player);
                        Move chosenMove = ai.selectMove(validMoves);
                        applyMove(game, player, chosenMove);
                    } else {
                        // Non-AI players (userC and userD) - use random moves for now
                        List<Move> validMoves = getValidMoves(game, player);
                        Move randomMove = validMoves.get((int) (Math.random() * validMoves.size()));
                        applyMove(game, player, randomMove);
                    }
                }
            }

            // Update AI's Q-values based on the game outcome
            ai.updateQValues(game);

            // Decay epsilon for epsilon-greedy strategy
            epsilonGreedy.decayEpsilon();

            // Log progress
            System.out.println("Episode " + episode + " | Epsilon: " + epsilonGreedy.getEpsilon());
        }

        // Save the trained AI to a file
        try {
            ai.save("trained_ai.ser");
            System.out.println("AI training results saved to trained_ai.ser");
        } catch (IOException e) {
            System.err.println("Failed to save AI state: " + e.getMessage());
        }
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

        boolean isFirstToPlay = game.getCurrentPlayerWithoutIterating().equals(player);

        if (isFirstToPlay) {
            // If leading the trick, AI can choose the trump suit (if not already set)
            if (trumpSuit == null) {
                for (Suit suit : Suit.values()) {
                    validMoves.add(new Move(null, suit)); // Choosing a trump suit
                }
            }
            // AI can play any card from their hand
            for (Card card : hand) {
                validMoves.add(new Move(card, null));
            }
        } else {
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

    private void applyMove(Game game, GamePlayer player, Move move) {
        Long gameId = game.getId();
        String playerName = player.getUser().getUsername();

        if (move.getCard() == null && move.getSuit() != null) {
            // Selecting the trump suit
            TrumpSuitSelectEvent selectEvent = new TrumpSuitSelectEvent(move.getSuit());
            activeGameService.selectSuit(gameId, selectEvent, playerName);

            // After selecting the trump suit, play a card
            List<Move> validMoves = getValidMoves(game, player);
            if (!validMoves.isEmpty()) {
                // Select the first valid move (or use AI logic to choose a move)
                Move cardMove = validMoves.get(0);
                if (cardMove.getCard() != null) {
                    CardSelectEvent cardEvent = new CardSelectEvent(cardMove.getCard().getId());
                    activeGameService.selectCard(gameId, cardEvent, playerName);
                }
            }
        } else if (move.getCard() != null) {
            // Playing a card
            CardSelectEvent cardEvent = new CardSelectEvent(move.getCard().getId());
            activeGameService.selectCard(gameId, cardEvent, playerName);
        }
    }
}
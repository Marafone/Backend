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
        MarafoneAI trainingAI = new MarafoneAI(epsilonGreedy, "Qvalue");

        // Load the previous trained AI for opponents
        MarafoneAI oldAI;
        try {
            oldAI = MarafoneAI.load("trained_ai_old.ser");
            System.out.println("Loaded previous trained AI from file.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load old AI. Falling back to random moves.");
            oldAI = null;
        }

        // Run the training loop for the specified number of episodes
        for (int episode = 0; episode < episodes; episode++) {
            // Create a new game
            CreateGameRequest createGameRequest = new CreateGameRequest("Training Game", GameType.MARAFFA, "ABC", 21);
            Long gameId = activeGameService.createGame(createGameRequest, userA);

            // Join players to the game
            JoinGameRequest joinRedTeam = new JoinGameRequest("");
            JoinGameRequest joinBlueTeam = new JoinGameRequest("");

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
                    List<Move> validMoves = getValidMoves(game, player);
                    Move chosenMove;

                    if (player.getUser().equals(userA) || player.getUser().equals(userB)) {
                        // AI players A and B (who are training)
                        chosenMove = trainingAI.selectMove(validMoves);
                    } else {
                        // AI players C and D (who use the old AI)
                        if (oldAI != null) {
                            chosenMove = oldAI.selectMove(validMoves);
                        } else {
                            // Fallback to random moves if old AI is not available
                            chosenMove = validMoves.get((int) (Math.random() * validMoves.size()));
                        }
                    }

                    if (chosenMove.getCard() == null && chosenMove.getSuit() != null) {
                        // The AI is leading the trick and needs to play two moves
                        MoveApplier.applyMove(game, player, chosenMove, activeGameService);
                        List<Move> validCards = getValidMoves(game, player);
                        Move chosenCard = player.getUser().equals(userA) || player.getUser().equals(userB)
                                ? trainingAI.selectMove(validCards)
                                : (oldAI != null ? oldAI.selectMove(validCards) : validCards.get((int) (Math.random() * validCards.size())));
                        MoveApplier.applyMove(game, player, chosenCard, activeGameService);
                    } else {
                        MoveApplier.applyMove(game, player, chosenMove, activeGameService);
                    }
                }
            }

            // Update AI's Q-values based on the game outcome
            trainingAI.updateQValues(game);

            // Decay epsilon for epsilon-greedy strategy
            epsilonGreedy.decayEpsilon();

            // Log progress
            System.out.println("Episode " + episode + " | Epsilon: " + epsilonGreedy.getEpsilon());
        }

        // Save the trained AI to a file
        try {
            trainingAI.save("trained_ai.ser");
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
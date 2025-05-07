package com.marafone.ai;

import com.marafone.marafone.game.model.*;
import java.util.*;
import java.io.*;

public class MarafoneAI implements Serializable {
    private static final long serialVersionUID = 1L;
    private final EpsilonGreedy epsilonGreedy;
    private final Map<StateActionPair, Double> qValues;
    private GameState currentState;
    private final int contextHistorySize = 3; // How many previous actions to consider

    public MarafoneAI(EpsilonGreedy epsilonGreedy, String saveFilePath) {
        this.epsilonGreedy = epsilonGreedy;
        this.qValues = loadQTable(saveFilePath);
        this.currentState = new GameState();
    }

    private Map<StateActionPair, Double> loadQTable(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Map<StateActionPair, Double>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();
        }
    }

    public void updateGameState(Game game) {
        List<Action> recentActions = Collections.emptyList();
        Suit trumpSuit = null;

        if (!game.getRounds().isEmpty()) {
            Round currentRound = game.getRounds().get(game.getRounds().size() - 1);
            trumpSuit = currentRound.getTrumpSuit();
            recentActions = currentRound.getLastNActions(contextHistorySize);
        }

        this.currentState = new GameState(
                recentActions,
                trumpSuit,
                game.getLeadingSuit(),
                game.getPlayersList(),
                game.getCurrentPlayerIndex(),
                game.getRoundNumber()
        );
    }

    public Move selectMove(List<Move> validMoves) {
        if (validMoves.isEmpty()) {
            throw new IllegalStateException("No valid moves available for AI");
        }

        // Log Q-values for debugging
        validMoves.forEach(m -> System.out.println("Move: " + m +
                " with state: " + currentState +
                " Q-Value: " + getQValue(m)));

        Move selectedMove;
        if (Math.random() < epsilonGreedy.getEpsilon()) {
            // Explore: choose a random move
            selectedMove = validMoves.get((int) (Math.random() * validMoves.size()));
        } else {
            // Exploit: choose the move with the highest Q-value for current state
            selectedMove = validMoves.stream()
                    .max((m1, m2) -> Double.compare(
                            getQValue(m1),
                            getQValue(m2)))
                    .orElseThrow();
        }

        // Store the state-action pair
        StateActionPair pair = new StateActionPair(currentState, selectedMove);
        qValues.putIfAbsent(pair, getQValue(selectedMove));

        return selectedMove;
    }

    public void updateQValues(Game game) {
        double reward = calculateReward(game);

        // Get the new state after the move
        updateGameState(game);
        GameState newState = this.currentState;

        // Update Q-values based on state transitions
        for (StateActionPair pair : qValues.keySet()) {
            if (pair.getState().equals(currentState)) {
                double oldQValue = qValues.get(pair);
                double maxNextQ = getMaxQValueForState(newState);
                double newQValue = oldQValue + 0.1 * (reward + 0.9 * maxNextQ - oldQValue);
                qValues.put(pair, newQValue);
            }
        }
    }

    private double getMaxQValueForState(GameState state) {
        return qValues.entrySet().stream()
                .filter(entry -> entry.getKey().getState().equals(state))
                .mapToDouble(Map.Entry::getValue)
                .max()
                .orElse(0.0);
    }

    private double getQValue(Move move) {
        StateActionPair pair = new StateActionPair(currentState, move);
        return qValues.computeIfAbsent(pair, p -> Math.random() * 0.1);
    }

    private double calculateReward(Game game) {
        Team aiTeam = Team.RED;
        int aiPoints = game.getTeamPoints(aiTeam);
        int opponentPoints = game.getTeamPoints(Team.BLUE);

        int pointDifference = aiPoints - opponentPoints;
        double reward = (double) pointDifference / game.getPointsToWinGame();

        if (game.getWinnerTeam() == aiTeam) {
            reward += 1.0;
        } else if (game.getWinnerTeam() != null) {
            reward -= 1.0;
        }

        return reward;
    }

    public void save(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    public static MarafoneAI load(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (MarafoneAI) ois.readObject();
        }
    }
}
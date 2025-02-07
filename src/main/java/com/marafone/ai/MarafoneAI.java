package com.marafone.ai;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.Team;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class MarafoneAI implements Serializable {
    private static final long serialVersionUID = 1L;
    private final EpsilonGreedy epsilonGreedy;
    private final Map<Move, Double> qValues; // Q-values for moves

    public MarafoneAI(EpsilonGreedy epsilonGreedy) {
        this.epsilonGreedy = epsilonGreedy;
        this.qValues = new HashMap<>();
    }

    public Move selectMove(List<Move> validMoves) {
        // Use epsilon-greedy strategy to select a move
        if (Math.random() < epsilonGreedy.getEpsilon()) {
            // Explore: choose a random move
            return validMoves.get((int) (Math.random() * validMoves.size()));
        } else {
            // Exploit: choose the move with the highest Q-value
            return validMoves.stream()
                    .max((m1, m2) -> Double.compare(getQValue(m1), getQValue(m2)))
                    .orElseThrow();
        }
    }

    public void updateQValues(Game game) {
        // Update Q-values based on the game outcome
        double reward = calculateReward(game);
        for (Move move : qValues.keySet()) {
            double oldQValue = qValues.get(move);
            double newQValue = oldQValue + 0.1 * (reward - oldQValue); // Learning rate = 0.1
            qValues.put(move, newQValue);
        }
    }

    private double calculateReward(Game game) {
        // Calculate the reward based on the game outcome
        if (game.getWinnerTeam() == Team.RED) {
            return 1.0; // Reward for winning
        } else {
            return -1.0; // Penalty for losing
        }
    }

    private double getQValue(Move move) {
        // Retrieve the Q-value for the move (default to 0 if not found)
        return qValues.getOrDefault(move, 0.0);
    }

    // Save the AI's state to a file
    public void save(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    // Load the AI's state from a file
    public static MarafoneAI load(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (MarafoneAI) ois.readObject();
        }
    }
}
package com.marafone.ai;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.Team;
import com.marafone.marafone.game.model.GamePlayer;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class MarafoneAI implements Serializable {
    private static final long serialVersionUID = 1L;
    private final EpsilonGreedy epsilonGreedy;
    private final Map<Move, Double> qValues; // Q-values for moves

    public MarafoneAI(EpsilonGreedy epsilonGreedy, String saveFilePath) {
        this.epsilonGreedy = epsilonGreedy;
        this.qValues = loadQTable(saveFilePath);
    }

    private Map<Move, Double> loadQTable(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Map<Move, Double>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>(); // If file doesn't exist, start fresh
        }
    }

    public Move selectMove(List<Move> validMoves) {
        if (validMoves.isEmpty()) {
            throw new IllegalStateException("No valid moves available for AI");
        }

        // Log Q-values for debugging
        validMoves.forEach(m -> System.out.println("Move: " + m + " Q-Value: " + getQValue(m)));

        Move selectedMove;
        if (Math.random() < epsilonGreedy.getEpsilon()) {
            // Explore: choose a random move
            selectedMove = validMoves.get((int) (Math.random() * validMoves.size()));
        } else {
            // Exploit: choose the move with the highest Q-value
            selectedMove = validMoves.stream()
                    .max((m1, m2) -> Double.compare(getQValue(m1), getQValue(m2)))
                    .orElseThrow();
        }

        // Ensure the move is stored for updating later
        qValues.putIfAbsent(selectedMove, getQValue(selectedMove));

        return selectedMove;
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
        // Get the AI's team (assuming the AI is on Team.RED)
        Team aiTeam = Team.RED;

        // Calculate the AI's points and the opponent's points
        int aiPoints = game.getPlayersList().stream()
                .filter(p -> p.getTeam() == aiTeam)
                .mapToInt(GamePlayer::getPoints)
                .sum();

        int opponentPoints = game.getPlayersList().stream()
                .filter(p -> p.getTeam() != aiTeam)
                .mapToInt(GamePlayer::getPoints)
                .sum();

        // Calculate the point difference
        int pointDifference = aiPoints - opponentPoints;

        // Normalize the point difference to a reward between -1 and 1
        double reward = (double) pointDifference / 21.0;

        // Add a bonus reward for winning the game
        if (game.getWinnerTeam() == aiTeam) {
            reward += 1.0; // Bonus for winning
        } else if (game.getWinnerTeam() != null) {
            reward -= 1.0; // Penalty for losing
        }

        return reward;
    }

    private double getQValue(Move move) {
        return qValues.computeIfAbsent(move, m -> Math.random() * 0.1); // Small random initialization
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
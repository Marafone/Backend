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
    private StateActionPair lastStateAction = null;

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
        List<Action> recentActions =  new LinkedList<>();
        Suit trumpSuit = null;

        if (!game.getRounds().isEmpty()) {
            Round currentRound = game.getRounds().getLast();
            trumpSuit = currentRound.getTrumpSuit();
            recentActions = currentRound.getLastNActions(currentRound.getActions().size() - 1);
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
        if (validMoves.isEmpty()) throw new IllegalStateException("No valid moves available for AI");

        Move selectedMove;
        if (Math.random() < epsilonGreedy.getEpsilon()) {
            selectedMove = validMoves.get((int) (Math.random() * validMoves.size()));
        } else {
            selectedMove = validMoves.stream()
                    .max(Comparator.comparingDouble(this::getQValue))
                    .orElseThrow();
        }

        lastStateAction = new StateActionPair(currentState, selectedMove); // 💡 Track move + state
        qValues.putIfAbsent(lastStateAction, getQValue(selectedMove));
        return selectedMove;
    }

    public void updateQValues(Game game) {
        if (lastStateAction == null) return; // No action to update

        double reward = calculateReward(game);
        updateGameState(game); // update currentState to new state
        GameState newState = this.currentState;

        double oldQValue = qValues.getOrDefault(lastStateAction, 0.0);
        double maxNextQ = getMaxQValueForState(newState);
        double newQValue = oldQValue + 0.1 * (reward + 0.9 * maxNextQ - oldQValue);

        qValues.put(lastStateAction, newQValue);
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
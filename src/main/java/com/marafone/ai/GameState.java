package com.marafone.ai;

import com.marafone.marafone.game.model.Action;
import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.model.Suit;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GameState implements Serializable {
    private final List<Action> recentActions;
    private final Suit trumpSuit;
    private final Suit leadingSuit;
    @Transient  // Add this annotation
    private final List<GamePlayer> players;
    private final int currentPlayerIndex;
    private final int roundNumber;

    public GameState(List<Action> recentActions, Suit trumpSuit, Suit leadingSuit,
                     List<GamePlayer> players, int currentPlayerIndex, int roundNumber) {
        this.recentActions = new ArrayList<>(recentActions);
        this.trumpSuit = trumpSuit;
        this.leadingSuit = leadingSuit;
        this.players = new ArrayList<>(players);
        this.currentPlayerIndex = currentPlayerIndex;
        this.roundNumber = roundNumber;
    }

    public GameState() {
        this(Collections.emptyList(), null, null, Collections.emptyList(), 0, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameState)) return false;
        GameState other = (GameState) o;
        return currentPlayerIndex == other.currentPlayerIndex &&
                roundNumber == other.roundNumber &&
                Objects.equals(recentActions, other.recentActions) &&
                trumpSuit == other.trumpSuit &&
                leadingSuit == other.leadingSuit; // <-- Remove players comparison
    }

    @Override
    public int hashCode() {
        return Objects.hash(recentActions, trumpSuit, leadingSuit, currentPlayerIndex, roundNumber); // <-- Remove players
    }

    @Override
    public String toString() {
        return "GameState{" +
                "recentActions=" + recentActions +
                ", trumpSuit=" + trumpSuit +
                ", leadingSuit=" + leadingSuit +
                ", roundNumber=" + roundNumber +
                ", currentPlayerIndex=" + currentPlayerIndex +
                '}';
    }
}


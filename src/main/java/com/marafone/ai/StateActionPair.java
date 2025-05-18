package com.marafone.ai;

import java.io.Serializable;
import java.util.Objects;

public class StateActionPair implements Serializable {
    private final GameState state;
    private final Move action;

    public StateActionPair(GameState state, Move action) {
        this.state = state;
        this.action = action;
    }

    public GameState getState() { return state; }
    public Move getAction() { return action; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StateActionPair)) return false;
        StateActionPair other = (StateActionPair) o;
        return Objects.equals(state, other.state) &&
                Objects.equals(action, other.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, action);
    }
}

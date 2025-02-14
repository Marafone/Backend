package com.marafone.ai;

import java.io.Serializable;

public class EpsilonGreedy implements Serializable {
    private static final long serialVersionUID = 2L;
    private double epsilon;
    private final double minEpsilon;
    private final double decayRate;

    public EpsilonGreedy(double initialEpsilon, double minEpsilon, double decayRate) {
        this.epsilon = initialEpsilon;
        this.minEpsilon = minEpsilon;
        this.decayRate = decayRate;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void decayEpsilon() {
        epsilon = Math.max(minEpsilon, epsilon * decayRate);
    }
}

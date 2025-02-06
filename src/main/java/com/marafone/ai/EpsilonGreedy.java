package com.marafone.ai;

public class EpsilonGreedy {
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

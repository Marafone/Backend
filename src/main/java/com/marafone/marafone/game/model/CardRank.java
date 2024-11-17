package com.marafone.marafone.game.model;

public enum CardRank implements Comparable<CardRank>{
    THREE(1),
    TWO(1),
    A(3),
    K(1),
    C(1),
    J(1),
    SEVEN(0),
    SIX(0),
    FIVE(0),
    FOUR(0);

    private final Integer points;

    CardRank(Integer points){
        this.points = points;
    }

    public Integer getPoints(){
        return points;
    }
}

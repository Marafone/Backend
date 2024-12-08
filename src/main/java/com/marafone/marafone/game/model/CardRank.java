package com.marafone.marafone.game.model;

public enum CardRank implements Comparable<CardRank>{
    FOUR(0),
    FIVE(0),
    SIX(0),
    SEVEN(0),
    J(1),
    C(1),
    K(1),
    A(3),
    TWO(1),
    THREE(1);

    private final Integer points;

    CardRank(Integer points){
        this.points = points;
    }

    public Integer getPoints(){
        return points;
    }
}

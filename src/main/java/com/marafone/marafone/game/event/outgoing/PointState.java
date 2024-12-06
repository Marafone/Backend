package com.marafone.marafone.game.event.outgoing;

import java.util.Map;

public final class PointState extends OutEvent{

    public Map<String, Integer> playerPointState; //username -> points

    public PointState(Map<String, Integer> playerPointState){
        super("PointState");
        this.playerPointState = playerPointState;
    }
}

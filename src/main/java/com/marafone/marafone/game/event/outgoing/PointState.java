package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Game;

import java.util.HashMap;
import java.util.Map;

public final class PointState extends OutEvent{

    public Map<String, Integer> playerPointState; //username -> points

    public PointState(Game game){
        super("PointState");

        playerPointState = new HashMap<>();

        for(var gamePlayer: game.getPlayersList()){
            playerPointState.put(gamePlayer.getUser().getUsername(), gamePlayer.getPoints());
        }
    }
}

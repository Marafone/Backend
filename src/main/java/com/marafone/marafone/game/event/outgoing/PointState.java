package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Team;

import java.util.Map;

public class PointState extends OutEvent{
    Map<String, Integer> playerPointState; //username -> points
}

package com.marafone.marafone.game.event.outgoing;

import lombok.Data;

import java.util.Map;

@Data
public class PointState extends OutEvent{
    Map<String, Integer> playerPointState; //username -> points
}

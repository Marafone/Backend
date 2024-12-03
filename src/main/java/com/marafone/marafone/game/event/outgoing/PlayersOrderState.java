package com.marafone.marafone.game.event.outgoing;

import lombok.Data;

import java.util.List;

@Data
public class PlayersOrderState extends OutEvent{
    List<String> playersOrder;
}


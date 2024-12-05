package com.marafone.marafone.game.event.outgoing;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PlayersOrderState extends OutEvent{
    List<String> playersOrder;
}


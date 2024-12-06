package com.marafone.marafone.game.event.outgoing;

import java.util.List;

public final class PlayersOrderState extends OutEvent{

    public List<String> playersOrder;

    public PlayersOrderState(List<String> playersOrder){
        super("PlayersOrderState");
        this.playersOrder = playersOrder;
    }
}


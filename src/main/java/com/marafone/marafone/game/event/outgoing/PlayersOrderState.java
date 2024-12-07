package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Game;

import java.util.List;

public final class PlayersOrderState extends OutEvent{

    public List<String> playersOrder;

    public PlayersOrderState(Game game){
        super("PlayersOrderState");
        this.playersOrder = game.getPlayersList()
                .stream().map(gamePlayer -> gamePlayer.getUser().getUsername()).toList();
    }
}


package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TeamState extends OutEvent{

    public List<String> redTeam;
    public List<String> blueTeam;

    public TeamState(Game game) {
        super("TeamState");

        redTeam = new ArrayList<>();
        blueTeam = new ArrayList<>();

        for(var gamePlayer: game.getPlayersList()){
            if (gamePlayer.getTeam() == Team.RED)
                redTeam.add(gamePlayer.getUser().getUsername());
            else
                blueTeam.add(gamePlayer.getUser().getUsername());
        }
    }
}

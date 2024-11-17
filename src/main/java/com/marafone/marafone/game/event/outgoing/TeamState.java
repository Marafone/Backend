package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Team;
import java.util.Map;

public class TeamState extends OutEvent{
    Map<String, Team> teamState; //username -> team
}

package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Team;
import lombok.Data;

import java.util.Map;

@Data
public class TeamState extends OutEvent{
    Map<String, Team> teamState; //username -> team
}

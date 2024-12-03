package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Team;
import lombok.Data;


@Data
public class WinnerState extends OutEvent{
    Team winnerTeam;
}

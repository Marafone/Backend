package com.marafone.marafone.game.random.order;

import com.marafone.marafone.game.model.GamePlayer;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MarafoneInitialOrderAssignerImpl implements RandomInitialOrderAssigner{

    /* In marafone player with four of coins card should be first */

    @Override
    public List<GamePlayer> assignRandomInitialOrder(List<GamePlayer> gamePlayers) {
        if(gamePlayers.size() != 4)
            throw new IllegalArgumentException("Expected 4 players");

        GamePlayer startingPlayer = gamePlayers.stream().filter(GamePlayer::hasFourOfCoins).findFirst().orElseThrow();

        LinkedList<GamePlayer> enemyTeam = gamePlayers.stream().filter(player -> player.getTeam() != startingPlayer.getTeam())
                .collect(Collectors.toCollection(LinkedList::new));

        LinkedList<GamePlayer> startingOrderOfPlayers = new LinkedList<>();
        startingOrderOfPlayers.add(startingPlayer);
        startingOrderOfPlayers.add(Math.random() < 0.5 ? enemyTeam.removeFirst() : enemyTeam.removeLast());
        startingOrderOfPlayers.add(gamePlayers.stream().filter(
                player -> player.getTeam() == startingPlayer.getTeam() && !player.equals(startingPlayer)
        ).findFirst().orElseThrow());
        startingOrderOfPlayers.add(enemyTeam.removeFirst());

        return startingOrderOfPlayers;
    }
}

package com.marafone.marafone.game.random.order;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.GamePlayer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

@Component
public class RandomInitialOrderAssignerImpl implements RandomInitialOrderAssigner{
    @Override
    public void assignRandomInitialOrder(Game game) {
        GamePlayer gamePlayer = game.getPlayersList().stream().filter(GamePlayer::hasFourOfCoins).findFirst().orElseThrow();

        LinkedList<GamePlayer> enemyTeam = game.getPlayersList().stream().filter(player -> player.getTeam() != gamePlayer.getTeam())
                .collect(Collectors.toCollection(LinkedList::new));

        LinkedList<GamePlayer> startingOrderOfPlayers = new LinkedList<>();
        startingOrderOfPlayers.add(gamePlayer);
        startingOrderOfPlayers.add(Math.random() < 0.5 ? enemyTeam.removeFirst() : enemyTeam.removeLast());
        startingOrderOfPlayers.add(game.getPlayersList().stream().filter(
                player -> player.getTeam() == gamePlayer.getTeam() && !player.equals(gamePlayer)
        ).findFirst().orElseThrow());
        startingOrderOfPlayers.add(enemyTeam.removeFirst());

        game.setPlayersList(startingOrderOfPlayers);
        game.setCurrentPlayer(startingOrderOfPlayers.listIterator());
        game.setInitialPlayersList(new ArrayList<>(game.getPlayersList()));
    }
}

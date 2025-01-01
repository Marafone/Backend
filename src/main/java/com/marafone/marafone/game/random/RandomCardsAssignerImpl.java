package com.marafone.marafone.game.random;

import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.GamePlayer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RandomCardsAssignerImpl implements RandomCardsAssigner{

    private final List<Card> allCards;

    @Override
    public void AssignRandomCardsToPlayers(List<GamePlayer> gamePlayers) {
        if(gamePlayers.size() > 4)
            throw new IllegalArgumentException("Expected at most 4 players");

        List<Card> cardsInRandomOrder = new ArrayList<>(allCards);

        Collections.shuffle(cardsInRandomOrder);

        int i = 0;
        for(var gamePlayer: gamePlayers){

            gamePlayer.setOwnedCards(new LinkedList<>());

            for(int j = 0; j < 10; j++){
                gamePlayer.getOwnedCards().add(cardsInRandomOrder.get(i * 10 + j));
            }

            i++;
        }
    }
}

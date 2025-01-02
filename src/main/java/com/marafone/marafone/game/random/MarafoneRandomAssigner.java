package com.marafone.marafone.game.random;

import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.model.Suit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MarafoneRandomAssigner implements RandomAssigner {

    private final List<Card> allCards;
    private final Random random;

    @Override
    public void assignRandomCardsToPlayers(List<GamePlayer> gamePlayers) {
        if(gamePlayers.size() > 4)
            throw new IllegalArgumentException("Expected at most 4 players");

        List<Card> cardsInRandomOrder = new ArrayList<>(allCards);

        Collections.shuffle(cardsInRandomOrder, random);

        int i = 0;
        for(var gamePlayer: gamePlayers){

            gamePlayer.setOwnedCards(new LinkedList<>());

            for(int j = 0; j < 10; j++){
                gamePlayer.getOwnedCards().add(cardsInRandomOrder.get(i * 10 + j));
            }

            i++;
        }
    }

    @Override
    public List<GamePlayer> assignRandomInitialOrder(List<GamePlayer> gamePlayers) {
        if(gamePlayers.size() != 4)
            throw new IllegalArgumentException("Expected 4 players");

        GamePlayer startingPlayer = gamePlayers.stream().filter(GamePlayer::hasFourOfCoins).findFirst().orElseThrow();

        LinkedList<GamePlayer> enemyTeam = gamePlayers.stream().filter(player -> player.getTeam() != startingPlayer.getTeam())
                .collect(Collectors.toCollection(LinkedList::new));

        LinkedList<GamePlayer> startingOrderOfPlayers = new LinkedList<>();
        startingOrderOfPlayers.add(startingPlayer);
        startingOrderOfPlayers.add(random.nextInt(2) == 1 ? enemyTeam.removeFirst() : enemyTeam.removeLast());
        startingOrderOfPlayers.add(gamePlayers.stream().filter(
                player -> player.getTeam() == startingPlayer.getTeam() && !player.equals(startingPlayer)
        ).findFirst().orElseThrow());
        startingOrderOfPlayers.add(enemyTeam.removeFirst());

        return startingOrderOfPlayers;
    }

    @Override
    public Card getRandomCorrectCard(List<Card> cards, Suit trumpSuit) {
        List<Card> cardsWithTrumpSuit = cards.stream().filter(card -> card.getSuit() == trumpSuit)
                .collect(Collectors.toCollection(ArrayList::new));

        if(!cardsWithTrumpSuit.isEmpty()){
            return cardsWithTrumpSuit.get(random.nextInt(cardsWithTrumpSuit.size()));
        }else{
            return cards.get(random.nextInt(cards.size()));
        }
    }

    @Override
    public Suit getRandomTrumpSuit() {
        return switch(random.nextInt(4)){
            case 0 -> Suit.CLUBS;
            case 1 -> Suit.SWORDS;
            case 2 -> Suit.COINS;
            default -> Suit.CUPS;
        };
    }
}

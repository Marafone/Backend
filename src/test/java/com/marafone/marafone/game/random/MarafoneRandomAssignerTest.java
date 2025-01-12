package com.marafone.marafone.game.random;

import com.marafone.marafone.game.active.ActiveGameServiceImpl;
import com.marafone.marafone.game.config.GameConfig;
import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.model.Suit;
import com.marafone.marafone.game.model.Team;
import com.marafone.marafone.mappers.GameMapper;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarafoneRandomAssignerTest {

    @Mock
    private Random random;

    @Mock
    private List<Card> allCards;

    @Mock
    private GamePlayer player1, player2, player3, player4;

    @Mock
    private Team team1, team2;

    @InjectMocks
    private MarafoneRandomAssigner randomAssigner;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.initMocks(this);  // Initialize mocks

        this.allCards = new GameConfig().allCards();

        // Set up getTeam() mock for each player
        when(player1.getTeam()).thenReturn(team1);
        when(player2.getTeam()).thenReturn(team2);
        when(player3.getTeam()).thenReturn(team1);
        when(player4.getTeam()).thenReturn(team2);

        // Other necessary setup
        List<GamePlayer> players = Arrays.asList(player1, player2, player3, player4);
        when(random.nextInt(2)).thenReturn(1);  // Mock random for team selection
    }

    @Test
    void testAssignRandomCardsToPlayers_moreThanFourPlayers_throwsException() {
        List<GamePlayer> gamePlayers = createGamePlayers(5);
        assertThrows(IllegalArgumentException.class, () -> randomAssigner.assignRandomCardsToPlayers(gamePlayers));
    }

    @Test
    void testAssignRandomInitialOrder_validPlayers() {
        List<GamePlayer> gamePlayers = createGamePlayers(4);

        // Mock `hasFourOfCoins` for one player
        GamePlayer startingPlayer = gamePlayers.get(0);
        when(startingPlayer.hasFourOfCoins()).thenReturn(true);

        // Mock team assignments for the players (ensure both teams are populated)
        when(gamePlayers.get(0).getTeam()).thenReturn(team1);
        when(gamePlayers.get(1).getTeam()).thenReturn(team2);
        when(gamePlayers.get(2).getTeam()).thenReturn(team1);
        when(gamePlayers.get(3).getTeam()).thenReturn(team2);

        List<GamePlayer> orderedPlayers = randomAssigner.assignRandomInitialOrder(gamePlayers);

        // Check that the starting player is the first in the list
        assertEquals(startingPlayer, orderedPlayers.get(0));

        // Ensure all players are included and unique in the order
        assertEquals(4, orderedPlayers.size());
        assertEquals(4, orderedPlayers.stream().distinct().count());
    }

    @Test
    void testAssignRandomInitialOrder_invalidNumberOfPlayers_throwsException() {
        List<GamePlayer> gamePlayers = createGamePlayers(3); // Less than 4 players
        assertThrows(IllegalArgumentException.class, () -> randomAssigner.assignRandomInitialOrder(gamePlayers));
    }

    @Test
    void testGetRandomCorrectCard() {
        List<Card> cards = allCards.subList(0, 5); // A subset of cards

        when(random.nextInt(cards.size())).thenReturn(2); // Mock random to return index 2
        Card randomCard = randomAssigner.getRandomCorrectCard(cards);

        assertEquals(cards.get(2), randomCard);
    }

    @Test
    void testGetRandomCorrectCard_withTrumpSuit() {

        // Mock random to pick the second trump card
        when(random.nextInt(2)).thenReturn(1);
        Card randomCard = randomAssigner.getRandomCorrectCard(allCards, Suit.CLUBS);

        assertEquals(Suit.CLUBS, randomCard.getSuit());
    }

    @Test
    void testGetRandomTrumpSuit() {
        when(random.nextInt(4)).thenReturn(2); // Mock random to return index 2
        Suit trumpSuit = randomAssigner.getRandomTrumpSuit();

        assertEquals(Suit.COINS, trumpSuit);
    }

    // Helper method to create game players
    private List<GamePlayer> createGamePlayers(int count) {
        List<GamePlayer> gamePlayers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            GamePlayer player = mock(GamePlayer.class);
            gamePlayers.add(player);
        }
        return gamePlayers;
    }
}

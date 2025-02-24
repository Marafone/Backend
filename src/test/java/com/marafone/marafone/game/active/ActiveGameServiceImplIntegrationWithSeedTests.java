package com.marafone.marafone.game.active;

import com.marafone.marafone.DummyData;
import com.marafone.marafone.exception.SelectCardException;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.game.response.JoinGameResult;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Random;

import static com.marafone.marafone.game.response.JoinGameResult.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class ActiveGameServiceImplIntegrationWithSeedTests {

    private final ActiveGameService activeGameService;
    private final ActiveGameRepository activeGameRepository;
    private final UserRepository userRepository;
    @Autowired
    private List<Card> allCards;

    @TestConfiguration
    static class ContextConfiguration {
        @Bean
        @Primary
        public Random randomWithSeed () {
            return new Random(83669323);
        }
    }

    @Autowired
    public ActiveGameServiceImplIntegrationWithSeedTests(ActiveGameService activeGameService, ActiveGameRepository activeGameRepository,
                                                         UserRepository userRepository){
        this.activeGameService = activeGameService;
        this.activeGameRepository = activeGameRepository;
        this.userRepository = userRepository;
    }

    @Test
    void testThatNewRoundStartsAfter40Moves(){
        //CREATE ACCOUNTS FOR USERS
        userRepository.save(DummyData.getUserA());
        userRepository.save(DummyData.getUserB());
        userRepository.save(DummyData.getUserC());
        userRepository.save(DummyData.getUserD());

        //CREATE GAME
        CreateGameRequest createGameRequest = new CreateGameRequest("name", GameType.MARAFFA, "ABC", 21);
        User owner = DummyData.getUserA();

        Long gameId = activeGameService.createGame(createGameRequest, owner);

        //JOIN AS TEAMMATE
        User ownerTeamMate = DummyData.getUserB();
        JoinGameRequest joinRedGoodCode = new JoinGameRequest(Team.RED, "ABC");

        JoinGameResult joined = activeGameService.joinGame(gameId, joinRedGoodCode, ownerTeamMate);

        assertEquals(SUCCESS, joined);

        //JOIN ENEMY TEAM
        User firstEnemy = DummyData.getUserC();
        User secondEnemy = DummyData.getUserD();
        JoinGameRequest joinBlueTeam = new JoinGameRequest(Team.BLUE, "ABC");
        activeGameService.joinGame(gameId, joinBlueTeam, firstEnemy);
        activeGameService.joinGame(gameId, joinBlueTeam, secondEnemy);

        //GETTERS FOR ASSERTING
        Game game = activeGameRepository.findById(gameId).get();
        GamePlayer ownerPlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(owner)).findFirst().get();
        GamePlayer ownerTeamMatePlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(ownerTeamMate)).findFirst().get();
        GamePlayer firstEnemyPlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(firstEnemy)).findFirst().get();
        GamePlayer secondEnemyPlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(secondEnemy)).findFirst().get();

        //START GAME
        activeGameService.startGame(gameId, owner.getUsername());
        assertTrue(game.hasStarted());

        //ASSERT CORRECT CARDS WERE GIVEN
        assertIterableEquals(
                List.of(
                        allCards.get(27), allCards.get(7), allCards.get(36), allCards.get(26), allCards.get(31),
                        allCards.get(1), allCards.get(22), allCards.get(39), allCards.get(18), allCards.get(29)
                ),
                ownerPlayer.getOwnedCards()
        );

        //ASSERT PROPER ORDER OF PLAYERS
        assertIterableEquals(
                List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer),
                game.getPlayersList()
        );

        //SELECT CARD WITHOUT TRUMP SUIT

        assertThrows(
            SelectCardException.class,
            () -> activeGameService.selectCard(gameId, new CardSelectEvent(28), owner.getUsername())
        );

        assertThrows(
            SelectCardException.class,
            () -> activeGameService.selectCard(gameId, new CardSelectEvent(29), secondEnemy.getUsername())
        );

        assertEquals(0, game.getRounds().getLast().getActions().size());

        //SELECT TRUMP SUIT BY BAD PLAYER

        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.COINS), secondEnemy.getUsername());
        assertNull(game.getRounds().getLast().getTrumpSuit());

        //SELECT TRUMP SUIT
        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.SWORDS), owner.getUsername());
        assertEquals(Suit.SWORDS, game.getRounds().getLast().getTrumpSuit());

        //FIRST TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(28), owner.getUsername()); //SIX COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(29), secondEnemy.getUsername()); //FIVE COINS

        assertThrows(
                SelectCardException.class,
                () -> activeGameService.selectCard(gameId, new CardSelectEvent(39), ownerTeamMate.getUsername())
        ); // BAD LEADING SUIT

        assertEquals(10, ownerTeamMatePlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(26), ownerTeamMate.getUsername());//J COINS
        assertEquals(9, ownerTeamMatePlayer.getOwnedCards().size());

        assertThrows(
                SelectCardException.class,
                () -> activeGameService.selectCard(gameId, new CardSelectEvent(24), ownerTeamMate.getUsername())
        ); //SELECTING CARD SECOND TIME IN SAME TURN

        assertEquals(9, ownerTeamMatePlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(22), firstEnemy.getUsername()); //TWO COINS

        assertEquals(2, firstEnemyPlayer.getPoints());
        assertIterableEquals(List.of(firstEnemyPlayer, ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer), game.getPlayersList());
        assertEquals(firstEnemyPlayer, game.getCurrentPlayerWithoutIterating());

        //SECOND TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(35), firstEnemy.getUsername()); //C CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(40), owner.getUsername());//FOUR CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(36), secondEnemy.getUsername());//J CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(39), ownerTeamMate.getUsername());//FIVE CLUBS
        assertEquals(4, firstEnemyPlayer.getPoints());
        assertIterableEquals(List.of(firstEnemyPlayer, ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer), game.getPlayersList());

        //THIRD TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(18), firstEnemy.getUsername());//SIX CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(19), owner.getUsername());//FIVE CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(11), secondEnemy.getUsername());//THREE CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(17), ownerTeamMate.getUsername());//SEVEN CUPS
        assertEquals(1, secondEnemyPlayer.getPoints());
        assertIterableEquals(List.of(secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer, ownerPlayer), game.getPlayersList());

        //FOURTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(5), secondEnemy.getUsername());//C SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(6), ownerTeamMate.getUsername());//J SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(1), firstEnemy.getUsername());//THREE SWORDS
        assertEquals(7, ownerPlayer.getOwnedCards().size());

        assertThrows(
            SelectCardException.class,
            () -> activeGameService.selectCard(gameId, new CardSelectEvent(27), owner.getUsername())
        ); //SEVEN COINS - should not work

        assertEquals(7, ownerPlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(8), owner.getUsername());//SIX SWORDS
        assertEquals(7, firstEnemyPlayer.getPoints());
        assertIterableEquals(List.of(firstEnemyPlayer, ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer), game.getPlayersList());

        //FIFTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(34), firstEnemy.getUsername());//K CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(37), owner.getUsername());//SEVEN CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(31), secondEnemy.getUsername());//THREE CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(38), ownerTeamMate.getUsername());//SIX CLUBS

        assertEquals(3, secondEnemyPlayer.getPoints());
        assertIterableEquals(List.of(secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer, ownerPlayer), game.getPlayersList());

        //SIXTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(33), secondEnemy.getUsername());//A CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(9), ownerTeamMate.getUsername());//FIVE SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(13), firstEnemy.getUsername());//A CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(32), owner.getUsername());//TWO CLUBS

        assertEquals(7, ownerTeamMatePlayer.getPoints());
        assertIterableEquals(List.of(ownerTeamMatePlayer, firstEnemyPlayer, ownerPlayer, secondEnemyPlayer), game.getPlayersList());

        //SEVENTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(12), ownerTeamMate.getUsername());//TWO CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(16), firstEnemy.getUsername());//J CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(27), owner.getUsername());//SEVEN COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(15), secondEnemy.getUsername());//C CUPS

        assertEquals(10, ownerTeamMatePlayer.getPoints());
        assertIterableEquals(List.of(ownerTeamMatePlayer, firstEnemyPlayer, ownerPlayer, secondEnemyPlayer), game.getPlayersList());

        //EIGHTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(25), ownerTeamMate.getUsername());//C COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(7), firstEnemy.getUsername());//SEVEN SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(23), owner.getUsername());//A COINS

        assertThrows(
                SelectCardException.class,
                () -> activeGameService.selectCard(gameId, new CardSelectEvent(20), secondEnemy.getUsername())
        ); // SHOULD DO NOTHING

        assertThrows(
                SelectCardException.class,
                () -> activeGameService.selectCard(gameId, new CardSelectEvent(3), secondEnemy.getUsername())
        ); // SHOULD DO NOTHING

        activeGameService.selectCard(gameId, new CardSelectEvent(21), secondEnemy.getUsername());//THREE COINS

        assertEquals(12, firstEnemyPlayer.getPoints());
        assertIterableEquals(List.of(firstEnemyPlayer, ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer), game.getPlayersList());

        //NINTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(10), firstEnemy.getUsername());//FOUR SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(2), owner.getUsername());//TWO SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(3), secondEnemy.getUsername());//A SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(14), ownerTeamMate.getUsername());//K CUPS

        assertEquals(5, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //PICK ENEMY CARDS (SHOULD DO NOTHING)

        assertThrows(
                SelectCardException.class,
                () -> activeGameService.selectCard(gameId, new CardSelectEvent(20), owner.getUsername())
        );

        assertThrows(
                SelectCardException.class,
                () -> activeGameService.selectCard(gameId, new CardSelectEvent(24), owner.getUsername())
        );

        assertThrows(
                SelectCardException.class,
                () -> activeGameService.selectCard(gameId, new CardSelectEvent(4), owner.getUsername())
        );

        assertEquals(1, ownerPlayer.getOwnedCards().size());

        activeGameService.selectCard(gameId, new CardSelectEvent(30), owner.getUsername());//FOUR COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(20), secondEnemy.getUsername());//FOUR CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(24), ownerTeamMate.getUsername());//K COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(4), firstEnemy.getUsername());//K SWORDS
        assertEquals((14 + 3) - (17 % 3), firstEnemyPlayer.getPoints());//bonus points for taking last trick

        //ASSERT NEW ROUND STARTS
        assertNull(game.getWinnerTeam());
        assertEquals(2, game.getRounds().size());
        for(var gamePlayer: game.getPlayersList()){
            assertEquals(10, gamePlayer.getOwnedCards().size());
        }
        assertIterableEquals(
                List.of(secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer, ownerPlayer),
                game.getPlayersList()
        );
        assertNull(game.getLeadingSuit());
        assertNull(game.getRounds().getLast().getTrumpSuit());

        //SHOULD DO NOTHING (TRUMP SUIT NOT SELECTED)
        for(int i = 1; i < 40; i++) {
            int cardIndex = i;
            assertThrows(
                    SelectCardException.class,
                    () -> activeGameService.selectCard(gameId, new CardSelectEvent(cardIndex), owner.getUsername())
            );
        }

        assertEquals(10, ownerPlayer.getOwnedCards().size());
        assertEquals(0, game.getRounds().getLast().getActions().size());

        //SELECT TRUMP SUIT
        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.COINS), owner.getUsername());// SHOULD DO NOTHING
        assertNull(game.getRounds().getLast().getTrumpSuit());
        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.CLUBS), secondEnemy.getUsername());
        assertEquals(Suit.CLUBS, game.getRounds().getLast().getTrumpSuit());

        //ELEVENTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(28), secondEnemy.getUsername());//SIX COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(25), ownerTeamMate.getUsername());//C COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(23), firstEnemy.getUsername());//A COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(26), owner.getUsername());//J COINS
        assertEquals(15 + 5, firstEnemyPlayer.getPoints()); // 5 points for this round and 15 from previous
        assertIterableEquals(List.of(firstEnemyPlayer, ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer), game.getPlayersList());
        assertEquals(4, game.getRounds().getLast().getActions().size());
    }


}

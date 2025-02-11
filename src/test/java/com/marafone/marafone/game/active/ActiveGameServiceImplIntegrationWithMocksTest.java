package com.marafone.marafone.game.active;

import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.config.GameConfig;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.game.random.RandomAssigner;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.LinkedList;
import java.util.List;

import static com.marafone.marafone.game.model.JoinGameResult.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class ActiveGameServiceImplIntegrationWithMocksTest {

    private final ActiveGameService activeGameService;
    private final ActiveGameRepository activeGameRepository;
    private final UserRepository userRepository;
    @Autowired
    private List<Card> allCards;

    @MockBean
    private RandomAssigner randomAssigner;

    @Autowired
    public ActiveGameServiceImplIntegrationWithMocksTest(ActiveGameService activeGameService, ActiveGameRepository activeGameRepository,
                                                 UserRepository userRepository) {
        this.activeGameService = activeGameService;
        this.activeGameRepository = activeGameRepository;
        this.userRepository = userRepository;
    }

    @Test
    void testThatGameEndsAfterRoundsEndsWithAchievedPoints() {

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

        //MOCK ASSIGNING CARDS
        doAnswer(invocationOnMock -> {

            ownerPlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(27), allCards.get(7), allCards.get(36), allCards.get(26), allCards.get(31),
                    allCards.get(1), allCards.get(22), allCards.get(39), allCards.get(18), allCards.get(29)
            )));

            ownerTeamMatePlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(8), allCards.get(16), allCards.get(25), allCards.get(23), allCards.get(5),
                    allCards.get(11), allCards.get(38), allCards.get(24), allCards.get(13), allCards.get(37)
            )));

            firstEnemyPlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(9), allCards.get(0), allCards.get(17), allCards.get(33), allCards.get(21),
                    allCards.get(34), allCards.get(12), allCards.get(15), allCards.get(6), allCards.get(3)
            )));

            secondEnemyPlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(32), allCards.get(35), allCards.get(19), allCards.get(14), allCards.get(10),
                    allCards.get(2), allCards.get(30), allCards.get(4), allCards.get(28), allCards.get(20)
            )));
            return null;
        })
        .when(randomAssigner).assignRandomCardsToPlayers(ArgumentMatchers.anyList());

        //MOCK ASSIGNING ORDER
        doAnswer(invocationOnMock -> new LinkedList<>(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer)))
        .when(randomAssigner).assignRandomInitialOrder(ArgumentMatchers.anyList());

        firstEnemyPlayer.setPoints(26);
        secondEnemyPlayer.setPoints(26);

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
        activeGameService.selectCard(gameId, new CardSelectEvent(28), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(29), secondEnemy.getUsername());
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
        activeGameService.selectCard(gameId, new CardSelectEvent(39), ownerTeamMate.getUsername());//BAD LEADING SUIT
        assertEquals(10, ownerTeamMatePlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(26), ownerTeamMate.getUsername());//J COINS
        assertEquals(9, ownerTeamMatePlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(24), ownerTeamMate.getUsername());//SELECTING CARD SECOND TIME IN SAME TURN
        assertEquals(9, ownerTeamMatePlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(22), firstEnemy.getUsername()); //TWO COINS

        assertEquals(28, firstEnemyPlayer.getPoints());
        assertIterableEquals(List.of(firstEnemyPlayer, ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer), game.getPlayersList());
        assertEquals(firstEnemyPlayer, game.getCurrentPlayerWithoutIterating());

        //SECOND TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(35), firstEnemy.getUsername()); //C CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(40), owner.getUsername());//FOUR CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(36), secondEnemy.getUsername());//J CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(39), ownerTeamMate.getUsername());//FIVE CLUBS
        assertEquals(30, firstEnemyPlayer.getPoints());
        assertIterableEquals(List.of(firstEnemyPlayer, ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer), game.getPlayersList());

        //THIRD TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(18), firstEnemy.getUsername());//SIX CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(19), owner.getUsername());//FIVE CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(11), secondEnemy.getUsername());//THREE CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(17), ownerTeamMate.getUsername());//SEVEN CUPS
        assertEquals(27, secondEnemyPlayer.getPoints());
        assertIterableEquals(List.of(secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer, ownerPlayer), game.getPlayersList());

        //FOURTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(5), secondEnemy.getUsername());//C SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(6), ownerTeamMate.getUsername());//J SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(1), firstEnemy.getUsername());//THREE SWORDS
        assertEquals(7, ownerPlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(27), owner.getUsername());//SEVEN COINS - should not work
        assertEquals(7, ownerPlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(8), owner.getUsername());//SIX SWORDS
        assertEquals(33, firstEnemyPlayer.getPoints());
        assertIterableEquals(List.of(firstEnemyPlayer, ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer), game.getPlayersList());

        //FIFTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(34), firstEnemy.getUsername());//K CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(37), owner.getUsername());//SEVEN CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(31), secondEnemy.getUsername());//THREE CLUBS
        activeGameService.selectCard(gameId, new CardSelectEvent(38), ownerTeamMate.getUsername());//SIX CLUBS

        assertEquals(29, secondEnemyPlayer.getPoints());
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
        activeGameService.selectCard(gameId, new CardSelectEvent(20), secondEnemy.getUsername());//SHOULD DO NOTHING
        activeGameService.selectCard(gameId, new CardSelectEvent(3), secondEnemy.getUsername());//SHOULD DO NOTHING
        activeGameService.selectCard(gameId, new CardSelectEvent(21), secondEnemy.getUsername());//THREE COINS

        assertEquals(38, firstEnemyPlayer.getPoints());
        assertIterableEquals(List.of(firstEnemyPlayer, ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer), game.getPlayersList());

        //NINTH TURN
        activeGameService.selectCard(gameId, new CardSelectEvent(10), firstEnemy.getUsername());//FOUR SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(2), owner.getUsername());//TWO SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(3), secondEnemy.getUsername());//A SWORDS
        activeGameService.selectCard(gameId, new CardSelectEvent(14), ownerTeamMate.getUsername());//K CUPS

        assertEquals(5, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //PICK ENEMY CARDS (SHOULD DO NOTHING)
        activeGameService.selectCard(gameId, new CardSelectEvent(20), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(24), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(4), owner.getUsername());
        assertEquals(1, ownerPlayer.getOwnedCards().size());

        activeGameService.selectCard(gameId, new CardSelectEvent(30), owner.getUsername());//FOUR COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(20), secondEnemy.getUsername());//FOUR CUPS
        activeGameService.selectCard(gameId, new CardSelectEvent(24), ownerTeamMate.getUsername());//K COINS
        activeGameService.selectCard(gameId, new CardSelectEvent(4), firstEnemy.getUsername());//K SWORDS
        assertEquals(43, firstEnemyPlayer.getPoints());//bonus points for taking last trick

        //ENEMY TEAM SHOULD WIN
        assertEquals(1, game.getRounds().size());
        assertEquals(Team.BLUE, game.getWinnerTeam());
    }

    @Test
    void testGamePlaysDifferentRoundsEndsAfterSetAmountOfPoints() {

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

        //MOCK ASSIGNING CARDS
        doAnswer(invocationOnMock -> {
            //here the card indexes go from 0 to 39
            // OWNER HAS ALL COINS
            ownerPlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(20), allCards.get(21), allCards.get(22), allCards.get(23), allCards.get(24),
                    allCards.get(25), allCards.get(26), allCards.get(27), allCards.get(28), allCards.get(29)
            )));
            // TEAMMATE ALL CUPS
            ownerTeamMatePlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(10), allCards.get(11), allCards.get(12), allCards.get(13), allCards.get(14),
                    allCards.get(15), allCards.get(16), allCards.get(17), allCards.get(18), allCards.get(19)
            )));
            // ENEMY ONE ALL SWORDS
            firstEnemyPlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(0), allCards.get(1), allCards.get(2), allCards.get(3), allCards.get(4),
                    allCards.get(5), allCards.get(6), allCards.get(7), allCards.get(8), allCards.get(9)
            )));
            // ENEMY TWO ALL CLUBS
            secondEnemyPlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(30), allCards.get(31), allCards.get(32), allCards.get(33), allCards.get(34),
                    allCards.get(35), allCards.get(36), allCards.get(37), allCards.get(38), allCards.get(39)
            )));
            return null;
        })
                .when(randomAssigner).assignRandomCardsToPlayers(ArgumentMatchers.anyList());

        //MOCK ASSIGNING ORDER
        doAnswer(invocationOnMock -> new LinkedList<>(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer)))
                .when(randomAssigner).assignRandomInitialOrder(ArgumentMatchers.anyList());

        firstEnemyPlayer.setPoints(0);
        secondEnemyPlayer.setPoints(0);

        //START GAME
        activeGameService.startGame(gameId, owner.getUsername());
        assertTrue(game.hasStarted());

        //ASSERT CORRECT CARDS WERE GIVEN
        assertIterableEquals(
                List.of(
                        allCards.get(20), allCards.get(21), allCards.get(22), allCards.get(23), allCards.get(24),
                        allCards.get(25), allCards.get(26), allCards.get(27), allCards.get(28), allCards.get(29)
                ),
                ownerPlayer.getOwnedCards()
        );

        //ASSERT PROPER ORDER OF PLAYERS
        assertIterableEquals(
                List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer),
                game.getPlayersList()
        );

        //SELECT CARD WITHOUT TRUMP SUIT
        activeGameService.selectCard(gameId, new CardSelectEvent(27), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(28), secondEnemy.getUsername());
        assertEquals(0, game.getRounds().getLast().getActions().size());

        //SELECT TRUMP SUIT BY BAD PLAYER
        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.COINS), secondEnemy.getUsername());
        assertNull(game.getRounds().getLast().getTrumpSuit());

        //SELECT TRUMP SUIT
        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.COINS), owner.getUsername());
        assertEquals(Suit.COINS, game.getRounds().getLast().getTrumpSuit());

        //FIRST TURN
        // EVERYONE PLAYS THE THREE OF THEIR SUIT
        activeGameService.selectCard(gameId, new CardSelectEvent(21), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(31), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(11), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(12), ownerTeamMate.getUsername());//SELECTING CARD SECOND TIME IN SAME TURN
        assertEquals(9, ownerTeamMatePlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(1), firstEnemy.getUsername());

        assertEquals(4, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //SECOND TURN
        // EVERYONE PLAYS THE TWO OF THEIR SUIT
        activeGameService.selectCard(gameId, new CardSelectEvent(22), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(32), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(12), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(2), firstEnemy.getUsername());

        assertEquals(8, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //THIRD TURN
        // PLAYING ACES
        activeGameService.selectCard(gameId, new CardSelectEvent(23), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(33), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(13), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(3), firstEnemy.getUsername());

        assertEquals(20, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //FOURTH TURN
        // PLAYING KINGS
        activeGameService.selectCard(gameId, new CardSelectEvent(24), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(34), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(14), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(4), firstEnemy.getUsername());

        assertEquals(24, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //FIFTH TURN
        // PLAYING HORSES
        activeGameService.selectCard(gameId, new CardSelectEvent(25), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(35), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(15), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(5), firstEnemy.getUsername());

        assertEquals(28, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //SIXTH TURN
        // PLAYING JACKS
        activeGameService.selectCard(gameId, new CardSelectEvent(26), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(36), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(16), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(6), firstEnemy.getUsername());

        assertEquals(32, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //SEVENTH TURN
        // PLAYING SEVENS
        activeGameService.selectCard(gameId, new CardSelectEvent(27), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(37), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(17), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(7), firstEnemy.getUsername());

        assertEquals(32, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //EIGHTH TURN
        // PLAYING SIXES
        activeGameService.selectCard(gameId, new CardSelectEvent(28), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(38), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(18), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(8), firstEnemy.getUsername());

        assertEquals(32, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //NINTH TURN
        // PLAYING FIVES
        activeGameService.selectCard(gameId, new CardSelectEvent(29), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(39), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(19), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(9), firstEnemy.getUsername());

        assertEquals(32, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //TENTH TURN
        // PLAYING FIVES
        activeGameService.selectCard(gameId, new CardSelectEvent(30), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(40), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(20), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(10), firstEnemy.getUsername());

        // GOES TO 35 WITH BONUS POINT BUT GETS ROUNDED TO 33, 11 IRL POINTS
        assertEquals(33, ownerPlayer.getPoints());
        assertIterableEquals(List.of(secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer, ownerPlayer), game.getPlayersList());

        //NEW ROUND SHOULD START
        assertEquals(2, game.getRounds().size());

        //SELECT TRUMP SUIT BY BAD PLAYER
        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.COINS), owner.getUsername());
        assertNull(game.getRounds().getLast().getTrumpSuit());

        //SELECT TRUMP SUIT BY CORRECT PLAYER
        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.COINS), secondEnemy.getUsername());
        assertEquals(Suit.COINS, game.getRounds().getLast().getTrumpSuit());

        //FIRST TURN
        // EVERYONE PLAYS THE THREE OF THEIR SUIT
        activeGameService.selectCard(gameId, new CardSelectEvent(31), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(11), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(12), ownerTeamMate.getUsername());//SELECTING CARD SECOND TIME IN SAME TURN
        assertEquals(9, ownerTeamMatePlayer.getOwnedCards().size());
        activeGameService.selectCard(gameId, new CardSelectEvent(1), firstEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(21), owner.getUsername());

        assertEquals(37, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //SECOND TURN
        // EVERYONE PLAYS THE Two OF THEIR SUIT
        activeGameService.selectCard(gameId, new CardSelectEvent(22), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(32), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(12), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(2), firstEnemy.getUsername());

        assertEquals(41, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //THIRD TURN
        // PLAYING ACES
        activeGameService.selectCard(gameId, new CardSelectEvent(23), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(33), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(13), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(3), firstEnemy.getUsername());

        assertEquals(53, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //FOURTH TURN
        // PLAYING KINGS
        activeGameService.selectCard(gameId, new CardSelectEvent(24), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(34), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(14), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(4), firstEnemy.getUsername());

        assertEquals(57, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //FIFTH TURN
        // PLAYING HORSES
        activeGameService.selectCard(gameId, new CardSelectEvent(25), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(35), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(15), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(5), firstEnemy.getUsername());

        assertEquals(61, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //SIXTH TURN
        // PLAYING JACKS
        activeGameService.selectCard(gameId, new CardSelectEvent(26), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(36), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(16), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(6), firstEnemy.getUsername());

        assertEquals(65, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //SEVENTH TURN
        // PLAYING SEVENS
        activeGameService.selectCard(gameId, new CardSelectEvent(27), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(37), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(17), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(7), firstEnemy.getUsername());

        assertEquals(65, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //EIGHTH TURN
        // PLAYING SIXES
        activeGameService.selectCard(gameId, new CardSelectEvent(28), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(38), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(18), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(8), firstEnemy.getUsername());

        assertEquals(65, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //NINTH TURN
        // PLAYING FIVES
        activeGameService.selectCard(gameId, new CardSelectEvent(29), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(39), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(19), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(9), firstEnemy.getUsername());

        assertEquals(65, ownerPlayer.getPoints());
        assertIterableEquals(List.of(ownerPlayer, secondEnemyPlayer, ownerTeamMatePlayer, firstEnemyPlayer), game.getPlayersList());

        //TENTH TURN
        // PLAYING FIVES
        activeGameService.selectCard(gameId, new CardSelectEvent(30), owner.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(40), secondEnemy.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(20), ownerTeamMate.getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(10), firstEnemy.getUsername());

        // GOES UP BECAUSE OF BONUS POINT AND DOESN'T GET ROUNDED DOWN BECAUSE GAME ENDS
        assertEquals(68, ownerPlayer.getPoints());

        //OWNER TEAM SHOULD WIN
        assertEquals(2, game.getRounds().size());
        assertEquals(Team.RED, game.getWinnerTeam());
    }

}

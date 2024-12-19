package com.marafone.marafone.game.active;

import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class ActiveGameServiceImplIntegrationTest {

    private final ActiveGameService activeGameService;
    private final ActiveGameRepository activeGameRepository;
    private final UserRepository userRepository;

    @Autowired
    public ActiveGameServiceImplIntegrationTest(ActiveGameService activeGameService, ActiveGameRepository activeGameRepository, UserRepository userRepository) {
        this.activeGameService = activeGameService;
        this.activeGameRepository = activeGameRepository;
        this.userRepository = userRepository;
    }

    @Test
    void fullGameFlowTest(){
        userRepository.save(DummyData.getUserA());
        userRepository.save(DummyData.getUserB());
        userRepository.save(DummyData.getUserC());
        userRepository.save(DummyData.getUserD());

        //CREATE GAME
        CreateGameRequest createGameRequest = new CreateGameRequest("name", GameType.MARAFFA, "ABC");
        User owner = DummyData.getUserA();

        Long gameId = activeGameService.createGame(createGameRequest, owner);

        //ASSERT PROPER OWNER
        Game game = activeGameRepository.findById(gameId).get();
        assertEquals(game.getOwner(), owner);

        //JOIN GAME - should fail
        User ownerTeamMate = DummyData.getUserB();
        JoinGameRequest joinRedWrongCode = new JoinGameRequest(Team.RED, "123");

        Boolean joined = activeGameService.joinGame(gameId, joinRedWrongCode, ownerTeamMate);

        assertFalse(joined);

        //JOIN GAME - should join
        JoinGameRequest joinRedGoodCode = new JoinGameRequest(Team.RED, "ABC");

        joined = activeGameService.joinGame(gameId, joinRedGoodCode, ownerTeamMate);

        assertTrue(joined);

        //JOIN ENEMY TEAM
        User firstEnemy = DummyData.getUserC();
        User secondEnemy = DummyData.getUserD();
        JoinGameRequest joinBlueTeam = new JoinGameRequest(Team.BLUE, "ABC");
        activeGameService.joinGame(gameId, joinBlueTeam, firstEnemy);
        activeGameService.joinGame(gameId, joinBlueTeam, secondEnemy);

        //ASSERT CORRECT PLAYERS
        boolean containsAll = game.getPlayersList().stream().map(player -> player.getUser().getUsername())
                .toList()
                .containsAll(
                        List.of(owner.getUsername(), ownerTeamMate.getUsername(),
                                firstEnemy.getUsername(), secondEnemy.getUsername()
                        )
                );
        assertTrue(containsAll);
        assertEquals(4, game.getPlayersList().size());

        //START GAME BY NOT OWNER - should not start
        activeGameService.startGame(gameId, firstEnemy.getUsername());

        assertFalse(game.hasStarted());

        //START GAME BY OWNER - should start
        activeGameService.startGame(gameId, owner.getUsername());

        assertTrue(game.hasStarted());

        //ASSERT 40 DISTINCT CARDS IN GAME
        List<Card> collectAllCards = game.getPlayersList().stream()
                .flatMap(x -> x.getOwnedCards().stream())
                .distinct().toList();

        assertEquals(40, collectAllCards.size());

        //ASSERT EVERY PLAYER HAS 10 CARDS
        for(var gamePlayer: game.getPlayersList())
            assertEquals(10, gamePlayer.getOwnedCards().size());

        //SELECT TRUMP SUIT
        GamePlayer hasFourOfCoins = game.getPlayersList().stream().filter(GamePlayer::hasFourOfCoins).findFirst().orElse(null);
        GamePlayer doesNotHaveFourOfCoins =  game.getPlayersList().stream().filter(x -> !x.hasFourOfCoins()).findFirst().orElse(null);
        assertNotNull(hasFourOfCoins);
        assertNotNull(doesNotHaveFourOfCoins);

        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.SWORDS), doesNotHaveFourOfCoins.getUser().getUsername());
        assertNull(game.getRounds().getLast().getTrumpSuit());

        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.SWORDS), hasFourOfCoins.getUser().getUsername());
        assertEquals(Suit.SWORDS, game.getRounds().getLast().getTrumpSuit());

        //ASSERT CORRECT PLAYERS ORDER
        Iterator<GamePlayer> playerIterator = game.getPlayersList().iterator();
        assertEquals(hasFourOfCoins, playerIterator.next());
        Team team = hasFourOfCoins.getTeam();
        assertNotEquals(team, playerIterator.next().getTeam());
        assertEquals(team, playerIterator.next().getTeam());
        assertNotEquals(team, playerIterator.next().getTeam());

        //MOCK CARDS FOR EASIER TESTING
        playerIterator = game.getPlayersList().iterator();
        playerIterator.next().setOwnedCards(
                new LinkedList<>(
                    List.of(
                            new Card(30L, CardRank.FOUR, Suit.COINS), new Card(18L, CardRank.SIX, Suit.CUPS)
                    )
                )
        );

        playerIterator.next().setOwnedCards(
                new LinkedList<>(
                        List.of(
                                new Card(35L, CardRank.C, Suit.CLUBS), new Card(13L, CardRank.A, Suit.CUPS)
                        )
                )
        );

        playerIterator.next().setOwnedCards(
                new LinkedList<>(
                        List.of(
                                new Card(7L, CardRank.SEVEN, Suit.SWORDS), new Card(24L, CardRank.K, Suit.COINS)
                        )
                )
        );

        playerIterator.next().setOwnedCards(
                new LinkedList<>(
                        List.of(
                                new Card(31L, CardRank.THREE, Suit.CLUBS), new Card(15L, CardRank.C, Suit.CUPS)
                        )
                )
        );
        playerIterator = game.getPlayersList().iterator();
        activeGameService.selectCard(gameId, new CardSelectEvent(30), playerIterator.next().getUser().getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(35), playerIterator.next().getUser().getUsername());
        GamePlayer shouldWinThisTurn = playerIterator.next();
        activeGameService.selectCard(gameId, new CardSelectEvent(24), shouldWinThisTurn.getUser().getUsername());//should be ignored
        activeGameService.selectCard(gameId, new CardSelectEvent(7), shouldWinThisTurn.getUser().getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(31), playerIterator.next().getUser().getUsername());

        assertEquals(4, game.getRounds().getLast().getActions().size());
        assertEquals(2, shouldWinThisTurn.getPoints());

        //ASSERT EVERYONE LOST ONE CARD AND HAVE CORRECT AMOUNT OF POINTS
        for(var gamePlayer: game.getPlayersList()){
            assertEquals(1, gamePlayer.getOwnedCards().size());
            if(!gamePlayer.equals(shouldWinThisTurn)){
                assertEquals(0, gamePlayer.getPoints());
            }
        }

        //NEXT TURN
        playerIterator = game.getPlayersList().iterator();
        activeGameService.selectCard(gameId, new CardSelectEvent(24), playerIterator.next().getUser().getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(15), playerIterator.next().getUser().getUsername());
        activeGameService.selectCard(gameId, new CardSelectEvent(18), playerIterator.next().getUser().getUsername());
        GamePlayer shouldGain8Points = playerIterator.next();
        activeGameService.selectCard(gameId, new CardSelectEvent(13), shouldGain8Points.getUser().getUsername());

        assertEquals(2, game.getRounds().size());
        assertEquals(8, game.getRounds().getFirst().getActions().size());
        assertEquals(8, shouldGain8Points.getPoints());

        //ASSERT CORRECT ORDER AFTER ROUND END
        playerIterator = game.getPlayersList().iterator();
        assertNotEquals(team, playerIterator.next().getTeam());//now enemy team is starting
        assertEquals(team, playerIterator.next().getTeam());
        assertNotEquals(team, playerIterator.next().getTeam());
        assertEquals(hasFourOfCoins, playerIterator.next()); // person who was first is now last

        //ASSERT EACH PLAYER HAS 10 CARDS AFTER NEW ROUND STARTS
        for(var gamePlayer: game.getPlayersList())
            assertEquals(10, gamePlayer.getOwnedCards().size());

        //ASSERT SELECTING CARD BEFORE SELECTING TRUMP SUIT DOES NOT WORK

        List<GamePlayer> curPlayersList = List.copyOf(game.getPlayersList());
        for(var gamePlayer: curPlayersList){
            List<Card> curOwnedCards = List.copyOf(gamePlayer.getOwnedCards());
            for(var card: curOwnedCards){
                activeGameService.selectCard(gameId, new CardSelectEvent(card.getId().intValue()), gamePlayer.getUser().getUsername());
            }
        }

        for(var gamePlayer: game.getPlayersList())
            assertEquals(10, gamePlayer.getOwnedCards().size());

        //MOCK CARDS
        List<Card> assignCards = List.of(
                new Card(7L, CardRank.SEVEN, Suit.SWORDS), new Card(24L, CardRank.K, Suit.COINS),
                new Card(30L, CardRank.FOUR, Suit.COINS), new Card(21L, CardRank.THREE, Suit.COINS)
        );
        Iterator<Card> cardIterator = assignCards.iterator();
        for(var gamePlayer: game.getPlayersList())
            gamePlayer.setOwnedCards(new LinkedList<>(List.of(cardIterator.next())));

        //SELECT TRUMP SUIT
        activeGameService.selectSuit(gameId, new TrumpSuitSelectEvent(Suit.COINS), game.getPlayersList().get(2).getUser().getUsername());

        //MOCK AMOUNT OF POINTS TO WIN
        game.pointsToWinGame = 3;

        //SELECT CARDS
        for(var gamePlayer: game.getPlayersList())
            activeGameService.selectCard(gameId,
                    new CardSelectEvent(gamePlayer.getOwnedCards().stream().map(Card::getId).findFirst().get().intValue()),
                    gamePlayer.getUser().getUsername()
            );

        //ASSERT THAT GAME WAS WON BY CORRECT TEAM
        assertEquals(shouldGain8Points.getTeam(), game.getWinnerTeam());
    }

}

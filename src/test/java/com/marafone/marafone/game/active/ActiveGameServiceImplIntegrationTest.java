package com.marafone.marafone.game.active;

import com.marafone.marafone.DummyData;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Iterator;
import java.util.List;

import static com.marafone.marafone.game.response.JoinGameResult.INCORRECT_PASSWORD;
import static com.marafone.marafone.game.response.JoinGameResult.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class ActiveGameServiceImplIntegrationTest {

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
    void gameFlowTestWithoutMocking(){
        userRepository.save(DummyData.getUserA());
        userRepository.save(DummyData.getUserB());
        userRepository.save(DummyData.getUserC());
        userRepository.save(DummyData.getUserD());

        //CREATE GAME
        CreateGameRequest createGameRequest = new CreateGameRequest("name", GameType.MARAFFA, "ABC", 21);
        User owner = DummyData.getUserA();

        Long gameId = activeGameService.createGame(createGameRequest, owner);

        //ASSERT PROPER OWNER
        Game game = activeGameRepository.findById(gameId).get();
        assertEquals(game.getOwner(), owner);

        //JOIN GAME - should fail
        User ownerTeamMate = DummyData.getUserB();
        JoinGameRequest joinRedWrongCode = new JoinGameRequest(Team.RED, "123");

        JoinGameResult joined = activeGameService.joinGame(gameId, joinRedWrongCode, ownerTeamMate);

        assertEquals(INCORRECT_PASSWORD, joined);

        //JOIN GAME - should join
        JoinGameRequest joinRedGoodCode = new JoinGameRequest(Team.RED, "ABC");

        joined = activeGameService.joinGame(gameId, joinRedGoodCode, ownerTeamMate);

        assertEquals(SUCCESS, joined);

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
    }

}

package com.marafone.marafone.game.active;

import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.config.CardConfig;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.game.random.cards.RandomCardsAssigner;
import com.marafone.marafone.game.random.order.RandomInitialOrderAssigner;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.marafone.marafone.game.model.JoinGameResult.INCORRECT_PASSWORD;
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
    private final List<Card> allCards;

    @MockBean
    private RandomCardsAssigner randomCardsAssigner;
    @MockBean
    private RandomInitialOrderAssigner randomInitialOrderAssigner;

    @Autowired
    public ActiveGameServiceImplIntegrationWithMocksTest(ActiveGameService activeGameService, ActiveGameRepository activeGameRepository,
                                                 UserRepository userRepository) {
        this.activeGameService = activeGameService;
        this.activeGameRepository = activeGameRepository;
        this.userRepository = userRepository;
        allCards = new CardConfig().allCards();
    }

    @Test
    void fullGameFlowTestWithMockedCards(){

        userRepository.save(DummyData.getUserA());
        userRepository.save(DummyData.getUserB());
        userRepository.save(DummyData.getUserC());
        userRepository.save(DummyData.getUserD());

        //CREATE GAME
        CreateGameRequest createGameRequest = new CreateGameRequest("name", GameType.MARAFFA, "ABC");
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

        //GETTERS USED IN MOCKS

        Game game = activeGameRepository.findById(gameId).get();
        GamePlayer ownerPlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(owner)).findFirst().get();
        GamePlayer teamMatePlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(ownerTeamMate)).findFirst().get();
        GamePlayer firstEnemyPlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(firstEnemy)).findFirst().get();
        GamePlayer secondEnemyPlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(secondEnemy)).findFirst().get();

        //MOCK ASSIGNING CARDS
        doAnswer(invocationOnMock -> {

            ownerPlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(3), allCards.get(7), allCards.get(15), allCards.get(20), allCards.get(25),
                    allCards.get(28), allCards.get(30), allCards.get(32), allCards.get(36), allCards.get(39)
            )));

            teamMatePlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(0), allCards.get(5), allCards.get(9), allCards.get(14), allCards.get(18),
                    allCards.get(22), allCards.get(26), allCards.get(31), allCards.get(34), allCards.get(38)
            )));

            firstEnemyPlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(1), allCards.get(4), allCards.get(8), allCards.get(11), allCards.get(17),
                    allCards.get(19), allCards.get(24), allCards.get(27), allCards.get(33), allCards.get(35)
            )));

            secondEnemyPlayer.setOwnedCards(new LinkedList<>(List.of(
                    allCards.get(2), allCards.get(6), allCards.get(10), allCards.get(13), allCards.get(16),
                    allCards.get(21), allCards.get(23), allCards.get(29), allCards.get(37), allCards.get(12)
            )));
            return null;
        })
        .when(randomCardsAssigner).assignRandomCardsToPlayers(ArgumentMatchers.anyList());

        //MOCK ASSIGNING ORDER

        doAnswer(invocationOnMock -> new LinkedList<>(List.of(ownerPlayer, firstEnemyPlayer, teamMatePlayer, secondEnemyPlayer)))
        .when(randomInitialOrderAssigner).assignRandomInitialOrder(ArgumentMatchers.anyList());

        //START GAME
        activeGameService.startGame(gameId, owner.getUsername());
        assertTrue(game.hasStarted());

        //ASSERT CORRECT CARDS WERE GIVEN
        assertIterableEquals(
                List.of(
                    allCards.get(1), allCards.get(4), allCards.get(8), allCards.get(11), allCards.get(17),
                    allCards.get(19), allCards.get(24), allCards.get(27), allCards.get(33), allCards.get(35)
                ),
                firstEnemyPlayer.getOwnedCards()
        );

        //ASSERT PROPER ORDER OF PLAYERS
        assertIterableEquals(
                List.of(ownerPlayer, firstEnemyPlayer, teamMatePlayer, secondEnemyPlayer),
                game.getPlayersList()
        );
    }

}

package com.marafone.marafone.game.active;

import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.config.GameConfig;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.model.*;
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

import static com.marafone.marafone.game.model.JoinGameResult.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class ActiveGameServiceImplIntegrationWithSeedTests {

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
            return new Random(100);
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
    void testThatSeedWorks(){
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

        //GETTERS FOR ASSERTING
        Game game = activeGameRepository.findById(gameId).get();
        GamePlayer ownerPlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(owner)).findFirst().get();
        GamePlayer teamMatePlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(ownerTeamMate)).findFirst().get();
        GamePlayer firstEnemyPlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(firstEnemy)).findFirst().get();
        GamePlayer secondEnemyPlayer = game.getPlayersList().stream().filter(player -> player.getUser().equals(secondEnemy)).findFirst().get();

        //START GAME
        activeGameService.startGame(gameId, owner.getUsername());
        assertTrue(game.hasStarted());

        //ASSERT CORRECT CARDS WERE GIVEN
        assertIterableEquals(
                List.of(
                        allCards.get(12), allCards.get(3), allCards.get(30), allCards.get(0), allCards.get(29),
                        allCards.get(32), allCards.get(26), allCards.get(23), allCards.get(15), allCards.get(21)
                ),
                ownerPlayer.getOwnedCards()
        );

        //ASSERT PROPER ORDER OF PLAYERS
        assertIterableEquals(
                List.of(ownerPlayer, secondEnemyPlayer, teamMatePlayer, firstEnemyPlayer),
                game.getPlayersList()
        );
    }
}

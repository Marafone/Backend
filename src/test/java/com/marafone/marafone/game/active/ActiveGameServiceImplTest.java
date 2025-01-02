package com.marafone.marafone.game.active;

import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.config.CardConfig;
import com.marafone.marafone.game.ended.EndedGameService;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.game.random.cards.RandomCardsAssignerImpl;
import com.marafone.marafone.game.random.order.RandomInitialOrderAssignerImpl;
import com.marafone.marafone.mappers.GameMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.marafone.marafone.game.model.JoinGameResult.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ActiveGameServiceImplTest {

    private ActiveGameServiceImpl activeGameServiceImpl;
    @Mock
    private ActiveGameRepository activeGameRepository;
    @Mock
    private EndedGameService endedGameService;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private List<Card> allCards;
    @Mock
    private GameMapper gameMapper;

    @BeforeEach
    void setUp(){
        activeGameServiceImpl = new ActiveGameServiceImpl(activeGameRepository, endedGameService, eventPublisher,
                new CardConfig().allCards(), gameMapper, new RandomCardsAssignerImpl(new CardConfig().allCards()),
                new RandomInitialOrderAssignerImpl());
    }

    // Create game tests

    @Test
    void testThatCreateGameReturnsCorrectGameId() {
        //given
        Mockito.when(activeGameRepository.put(any())).thenReturn(2L);

        //when
        Long gameId = activeGameServiceImpl.createGame(DummyData.getCreateGameRequestA(), DummyData.getUserA());

        //then
        assertEquals(2L, gameId);
    }

    //Join game tests

    @Test
    void joiningNotFullTeamShouldReturnSuccess(){
        //given
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(DummyData.getGameInLobby()));

        //when
        JoinGameResult joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserB());

        //then
        assertSame(JoinGameResult.SUCCESS, joined);
    }

    @Test
    void joiningFullTeamsShouldReturnTeamsFullMsg(){
        //given
        Game sampleGame = DummyData.getGameInLobby();
        sampleGame.getPlayersList().add(DummyData.getGamePlayerRedB());
        sampleGame.getPlayersList().add(DummyData.getGamePlayerRedA());
        sampleGame.getPlayersList().add(DummyData.getGamePlayerBlueA());
        sampleGame.getPlayersList().add(DummyData.getGamePlayerBlueB());
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(sampleGame));

        //when
        JoinGameResult joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserC());

        //then
        assertEquals(TEAMS_FULL, joined);
    }

    @Test
    void joiningNotExistentGameShouldReturnGameNotFoundMsg(){
        //given
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.empty());

        //when
        JoinGameResult joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserA());

        //then
        assertEquals(GAME_NOT_FOUND, joined);
    }

    @Test
    void joiningGameWithBadJoinCodeShouldReturnIncorrectPasswordMsg(){
        //given
        Game sampleGame = DummyData.getGameInLobby();
        sampleGame.setJoinGameCode("123");
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(sampleGame));

        //when
        JoinGameResult joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, "ABC"), DummyData.getUserB());

        //then
        assertEquals(INCORRECT_PASSWORD, joined);
    }

    @Test
    void joiningSameGameTwoTimesShouldReturnPlayerAlreadyJoinedMsg(){
        //given
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(DummyData.getGameInLobby()));

        //when
        JoinGameResult joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserB());
        JoinGameResult joinedAgain = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserB());

        //then
        assertEquals(SUCCESS, joined);
        assertEquals(PLAYER_ALREADY_JOINED, joinedAgain);
    }

    //Starting game tests

    @Test
    void startingGameWithNotFullLobbyShouldNotStart(){
        //given
        Game sampleGame = DummyData.getGameInLobby();
        Mockito.when(activeGameRepository.findById(1L)).thenReturn(Optional.ofNullable(sampleGame));

        //when
        activeGameServiceImpl.startGame(1L, sampleGame.getOwner().getUsername());

        //then
        assertNull(sampleGame.getStartedAt());
    }

    @Test
    void startingGameByNotOwnerShouldNotStart(){
        //given
        Game sampleGame = DummyData.getGameInLobby();
        sampleGame.setPlayersList(new LinkedList<>(List.of(
                DummyData.getGamePlayerRedA(), DummyData.getGamePlayerRedB(),
                DummyData.getGamePlayerBlueA(), DummyData.getGamePlayerBlueB()
        )));
        Mockito.when(activeGameRepository.findById(1L)).thenReturn(Optional.ofNullable(sampleGame));

        //when
        activeGameServiceImpl.startGame(1L, DummyData.getGamePlayerRedB().getUser().getUsername());

        //then
        assertNull(sampleGame.getStartedAt());
    }

    @Test
    void startingGameByOwnerWithFullLobbyShouldStart(){
        //given
        Game sampleGame = DummyData.getGameInLobby();
        assertNull(sampleGame.getStartedAt());
        sampleGame.setPlayersList(new LinkedList<>(List.of(
                DummyData.getGamePlayerRedA(), DummyData.getGamePlayerRedB(),
                DummyData.getGamePlayerBlueA(), DummyData.getGamePlayerBlueB()
        )));
        Mockito.when(activeGameRepository.findById(1L)).thenReturn(Optional.ofNullable(sampleGame));

        //when
        activeGameServiceImpl.startGame(1L, sampleGame.getOwner().getUsername());

        //then
        assertNotNull(sampleGame.getStartedAt());
    }

    //Winning action tests

    @Test
    void testWinningActionWithSameSuit(){
        //given
        List<Action> currentTurn = new LinkedList<>();
        Action weakAction = Action.builder().id(1L).card(Card.builder().suit(Suit.SWORDS).rank(CardRank.K).build()).build();
        Action strongAction = Action.builder().id(2L).card(Card.builder().suit(Suit.SWORDS).rank(CardRank.A).build()).build();

        Round round = Round.builder()
                .actions(new LinkedList<>(List.of(strongAction, weakAction)))
                .trumpSuit(Suit.SWORDS)
                .build();

        weakAction.setRound(round);
        strongAction.setRound(round);

        currentTurn.add(weakAction);
        currentTurn.add(strongAction);

        //when
        Action winningAction = activeGameServiceImpl.getWinningAction(currentTurn);

        //then
        assertEquals(strongAction, winningAction);
    }

    @Test
    void testWinningActionWithDiffrentSuit(){
        //given
        List<Action> currentTurn = new LinkedList<>();
        Action weakAction = Action.builder().id(1L).card(Card.builder().suit(Suit.CUPS).rank(CardRank.A).build()).build();
        Action strongAction = Action.builder().id(2L).card(Card.builder().suit(Suit.SWORDS).rank(CardRank.THREE).build()).build();

        Round round = Round.builder()
                .actions(new LinkedList<>(List.of(strongAction, weakAction)))
                .trumpSuit(Suit.SWORDS)
                .build();

        weakAction.setRound(round);
        strongAction.setRound(round);

        currentTurn.add(weakAction);
        currentTurn.add(strongAction);

        //when
        Action winningAction = activeGameServiceImpl.getWinningAction(currentTurn);

        //then
        assertEquals(strongAction, winningAction);
    }

    @Test
    void testGameExistenceWhenGameAlreadyExist() {
        // given
        Game queuedGame = new Game();
        queuedGame.setName("game");
        List<Game> queuedGames = List.of(queuedGame);
        String newGameToAddName = "game";
        Mockito.when(activeGameRepository.getWaitingGames()).thenReturn(queuedGames);

        // when
        boolean result = activeGameServiceImpl.doesNotStartedGameAlreadyExist(newGameToAddName);

        // then
        assertTrue(result);
    }

    @Test
    void testGameExistenceWhenGameDoesNotExist() {
        // given
        Game queuedGame = new Game();
        queuedGame.setName("game");
        List<Game> queuedGames = List.of(queuedGame);
        String newGameToAddName = "otherGame";
        Mockito.when(activeGameRepository.getWaitingGames()).thenReturn(queuedGames);

        // when
        boolean result = activeGameServiceImpl.doesNotStartedGameAlreadyExist(newGameToAddName);

        // then
        assertFalse(result);
    }

    @Test
    void getGameTeams_WhenGameDoesNotExist_ShouldReturnNull() {
        // given
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.empty());

        // when
        Map<Team, List<GamePlayer>> result = activeGameServiceImpl.getGameTeams(1L);

        // then
        assertNull(result);
    }

    @Test
    void getGameTeams_WhenGameExistsWithTwoPlayers_ShouldReturnMapWithTwoPlayers() {
        // given
        GamePlayer gp1 = GamePlayer.builder()
                .id(1L)
                .team(Team.RED)
                .build();
        GamePlayer gp2 = GamePlayer.builder()
                .id(2L)
                .team(Team.BLUE)
                .build();
        Game game = Game.builder()
                .id(1L)
                .playersList(List.of(gp1, gp2))
                .build();
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(game));

        // when
        Map<Team, List<GamePlayer>> result = activeGameServiceImpl.getGameTeams(1L);

        // then
        assertNotNull(result);
        assertNotNull(result.get(Team.RED));
        assertNotNull(result.get(Team.BLUE));
        assertEquals(1, result.get(Team.RED).size());
        assertEquals(1, result.get(Team.BLUE).size());
        assertEquals(gp1.getId(), result.get(Team.RED).getFirst().getId());
        assertEquals(gp2.getId(), result.get(Team.BLUE).getFirst().getId());
    }

    @Test
    void getGameTeams_WhenGameIsFull_ShouldReturnMapWithAllPlayers() {
        // given
        GamePlayer gp1 = GamePlayer.builder()
                .id(1L)
                .team(Team.RED)
                .build();
        GamePlayer gp2 = GamePlayer.builder()
                .id(2L)
                .team(Team.BLUE)
                .build();
        GamePlayer gp3 = GamePlayer.builder()
                .id(3L)
                .team(Team.RED)
                .build();
        GamePlayer gp4 = GamePlayer.builder()
                .id(4L)
                .team(Team.BLUE)
                .build();
        Game game = Game.builder()
                .id(1L)
                .playersList(List.of(gp1, gp2, gp3, gp4))
                .build();
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(game));

        // when
        Map<Team, List<GamePlayer>> result = activeGameServiceImpl.getGameTeams(1L);

        // then
        assertNotNull(result);
        assertNotNull(result.get(Team.RED));
        assertNotNull(result.get(Team.BLUE));
        assertEquals(2, result.get(Team.RED).size());
        assertEquals(2, result.get(Team.BLUE).size());
    }

}
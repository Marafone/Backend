package com.marafone.marafone.game.active;

import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.config.GameConfig;
import com.marafone.marafone.game.ended.EndedGameService;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.game.random.MarafoneRandomAssigner;
import com.marafone.marafone.mappers.GameMapper;
import com.marafone.marafone.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;

import java.util.*;

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
                new GameConfig().allCards(), gameMapper, new MarafoneRandomAssigner(new GameConfig().allCards(), new GameConfig().random()));
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
    void leaveGame_WhenUserInGame_ShouldRemoveUserFromGamePlayersList() {
        // given
        User user = new User(1L, "user1", "", "user1");
        GamePlayer gp1 = GamePlayer
                .builder()
                .user(new User(2L, "user2", "", "user2"))
                .build();
        GamePlayer gp2 = GamePlayer
                .builder()
                .user(user)
                .build();
        GamePlayer gp3 = GamePlayer
                .builder()
                .user(new User(3L, "user3", "", "user3"))
                .build();

        List<GamePlayer> playersList = new ArrayList<>();
        playersList.add(gp1);
        playersList.add(gp2);
        playersList.add(gp3);

        Game game = Game.builder().playersList(playersList).owner(user).id(1L).build();
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(game));

        // when
        activeGameServiceImpl.leaveGame(1L, user);

        // then
        assertEquals(2, game.getPlayersList().size());
        assertFalse(game.getPlayersList().contains(gp2));
    }

    @Test
    void leaveGame_WhenUserNotInGame_ShouldDoNothing() {
        // given
        User user = new User(1L, "user1", "", "user1");
        GamePlayer gp1 = GamePlayer
                .builder()
                .user(new User(2L, "user2", "", "user2"))
                .build();
        GamePlayer gp2 = GamePlayer
                .builder()
                .user(new User(3L, "user3", "", "user3"))
                .build();

        List<GamePlayer> playersList = new ArrayList<>();
        playersList.add(gp1);
        playersList.add(gp2);

        Game game = Game.builder().playersList(playersList).id(1L).build();
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(game));

        // when
        activeGameServiceImpl.leaveGame(1L, user);

        // then
        assertEquals(2, game.getPlayersList().size());
    }

    @Test
    void changeTeam_WhenTeamNotFull_ShouldChangeTeams() {
        // given
        List<User> users = Arrays.asList(
                new User(1L, "user1", "", "user1"),
                new User(2L, "user2", "", "user2"),
                new User(3L, "user3", "", "user3")
        );
        List<GamePlayer> gamePlayersList = Arrays.asList(
            new GamePlayer(1L, users.get(0), Team.RED, null, null),
            new GamePlayer(2L, users.get(1), Team.RED, null, null),
            new GamePlayer(3L, users.get(2), Team.BLUE, null, null)
        );
        Game game = Game.builder().id(1L).playersList(gamePlayersList).build();

        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(game));

        // when
        activeGameServiceImpl.changeTeam(1L, Team.BLUE, users.getFirst());

        // then
        assertEquals(Team.BLUE, gamePlayersList.getFirst().getTeam());
        assertTrue(game.teamIsFull(Team.BLUE));
        assertFalse(game.teamIsFull(Team.RED));
    }

    @Test
    void changeTeam_WhenTeamFull_ShouldNotAllowUserToChangeTeam() {
        // given
        List<User> users = Arrays.asList(
                new User(1L, "user1", "", "user1"),
                new User(2L, "user2", "", "user2"),
                new User(3L, "user3", "", "user3"),
                new User(4L, "user4", "", "user4")
        );
        List<GamePlayer> gamePlayersList = Arrays.asList(
                new GamePlayer(1L, users.get(0), Team.RED, null, null),
                new GamePlayer(2L, users.get(1), Team.RED, null, null),
                new GamePlayer(3L, users.get(2), Team.BLUE, null, null),
                new GamePlayer(4L, users.get(3), Team.BLUE, null, null)
        );
        Game game = Game.builder().id(1L).playersList(gamePlayersList).build();

        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(game));

        // when
        activeGameServiceImpl.changeTeam(1L, Team.BLUE, users.get(1));

        // then
        assertEquals(Team.RED, gamePlayersList.get(1).getTeam());
    }

    @Test
    void changeTeam_WhenTargetTeamIsSameAsCurrentTeam_ShouldNotChangeTeams() {
        // given
        List<User> users = Arrays.asList(
                new User(1L, "user1", "", "user1"),
                new User(2L, "user2", "", "user2")
        );
        List<GamePlayer> gamePlayersList = Arrays.asList(
                new GamePlayer(1L, users.get(0), Team.RED, null, null),
                new GamePlayer(2L, users.get(1), Team.BLUE, null, null)
        );
        Game game = Game.builder().id(1L).playersList(gamePlayersList).build();

        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(game));

        // when
        activeGameServiceImpl.changeTeam(1L, Team.BLUE, users.getLast());

        // then
        assertEquals(Team.BLUE, gamePlayersList.getLast().getTeam());
    }

    @Test
    void startingGameWithNoPlayersShouldNotStart() {
        // given
        Game sampleGame = DummyData.getGameInLobby();
        sampleGame.setPlayersList(new LinkedList<>()); // No players in the game
        Mockito.when(activeGameRepository.findById(1L)).thenReturn(Optional.of(sampleGame));

        // when
        activeGameServiceImpl.startGame(1L, sampleGame.getOwner().getUsername());

        // then
        assertNull(sampleGame.getStartedAt());
    }

    @Test
    void joiningGameAfterItHasStartedShouldReturnGameAlreadyStarted() {
        // given
        Game sampleGame = DummyData.getGameInLobby();
        sampleGame.setStartedAt(LocalDateTime.now());
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(sampleGame));

        // when
        JoinGameResult result = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserA());

        // then
        assertEquals(GAME_ALREADY_STARTED, result);
    }

    @Test
    void changeTeam_WhenUserNotInGame_ShouldDoNothing() {
        // given
        User user = new User(1L, "user1", "", "user1");
        Game game = Game.builder()
                .playersList(new LinkedList<>(List.of(
                        new GamePlayer(2L, new User(2L, "user2", "", "user2"), Team.RED, null, null)
                )))
                .id(1L)
                .build();
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(game));

        // when
        activeGameServiceImpl.changeTeam(1L, Team.BLUE, user);

        // then
        assertEquals(1, game.getPlayersList().size());
        assertEquals(Team.RED, game.getPlayersList().get(0).getTeam());
    }
}
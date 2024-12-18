package com.marafone.marafone.game.active;
import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.config.CardConfig;
import com.marafone.marafone.game.ended.EndedGameService;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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

    @BeforeEach
    void setUp(){
        activeGameServiceImpl = new ActiveGameServiceImpl(activeGameRepository, endedGameService, eventPublisher,
                new CardConfig().allCards());
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
    void joiningNotFullTeamShouldReturnTrue(){
        //given
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(DummyData.getGameInLobby()));

        //when
        Boolean joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserB());

        //then
        assertTrue(joined);
    }

    @Test
    void joiningFullTeamShouldReturnFalse(){
        //given
        Game sampleGame = DummyData.getGameInLobby();
        sampleGame.getPlayersList().add(DummyData.getGamePlayerRedB());
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(sampleGame));

        //when
        Boolean joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserC());

        //then
        assertFalse(joined);
    }

    @Test
    void joiningNotExistentGameShouldReturnFalse(){
        //given
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.empty());

        //when
        Boolean joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserA());

        //then
        assertFalse(joined);
    }

    @Test
    void joiningGameWithBadJoinCodeShouldReturnFalse(){
        //given
        Game sampleGame = DummyData.getGameInLobby();
        sampleGame.setJoinGameCode("123");
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(sampleGame));

        //when
        Boolean joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, "ABC"), DummyData.getUserB());

        //then
        assertFalse(joined);
    }

    @Test
    void joiningSameGameTwoTimesShouldReturnFalse(){
        //given
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.of(DummyData.getGameInLobby()));

        //when
        Boolean joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserB());
        Boolean joinedAgain = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserB());

        //then
        assertTrue(joined);
        assertFalse(joinedAgain);
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

}
package com.marafone.marafone.game.active;
import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.config.CardConfig;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.Team;
import com.marafone.marafone.user.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private EventPublisher eventPublisher;
    @Mock
    private UserRepository userRepository;
    @Mock
    private List<Card> allCards;

    @BeforeEach
    void setUp(){
        activeGameServiceImpl = new ActiveGameServiceImpl(activeGameRepository, eventPublisher, userRepository,
                new CardConfig().allCards());
    }

    @Test
    void createGame() {
        //given
        Mockito.when(activeGameRepository.put(any())).thenReturn(2L);
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.ofNullable(DummyData.getUserA()));

        //when
        Long gameId = activeGameServiceImpl.createGame(DummyData.getCreateGameRequestA(), DummyData.getUserA().getUsername());

        //then
        assertEquals(2L, gameId);
    }

    @Test
    void joiningNotFullTeamShouldReturnTrue(){
        //given
        var game = DummyData.getGameInProgress();
        game.setStartedAt(null);
        Mockito.when(activeGameRepository.findById(any())).thenReturn(Optional.ofNullable(game));
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.ofNullable(DummyData.getUserB()));

        //when
        Boolean joined = activeGameServiceImpl.joinGame(1L, new JoinGameRequest(Team.RED, null), DummyData.getUserB().getUsername());

        //then
        assertEquals(true, joined);
    }

    /* TODO implement tests for joining game

    @Test
    void joiningFullTeamShouldReturnFalse(){

    }

    @Test
    void joiningNotExistentGameShouldThrowException(){

    }

    @Test
    void joiningGameWithBadJoinCodeShouldReturnFalse(){

    }

    @Test
    void joiningSameGameTwoTimesShouldReturnFalse(){

    }

     */

    @Test
    void startingGameWithNotFullLobbyShouldNotStart(){
        //given
        Game sampleGame = DummyData.getGameInLobby();
        Mockito.when(activeGameRepository.findById(1L)).thenReturn(Optional.ofNullable(sampleGame));

        //when
        activeGameServiceImpl.startGame(1L, sampleGame.getOwner().getUser().getUsername());

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
        activeGameServiceImpl.startGame(1L, sampleGame.getOwner().getUser().getUsername() + "123");

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
        activeGameServiceImpl.startGame(1L, sampleGame.getOwner().getUser().getUsername());

        //then
        assertNotNull(sampleGame.getStartedAt());
    }

}
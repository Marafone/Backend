package com.marafone.marafone.game.active;
import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.user.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ActiveGameServiceImplTest {

    @InjectMocks
    private ActiveGameServiceImpl activeGameServiceImpl;
    @Mock
    private ActiveGameRepository activeGameRepository;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private UserRepository userRepository;

    @Test
    void createGame() {
        //given
        Mockito.when(activeGameRepository.put(any())).thenReturn(2L);
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.ofNullable(DummyData.getUserA()));

        //when
        Long gameId = activeGameServiceImpl.createGame(DummyData.getCreateGameRequestA(), "Norbert");

        //then
        assertEquals(2L, gameId);}
}
package com.marafone.marafone.game.active;

import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActiveGameRepositoryImplTest {

    private ActiveGameRepositoryImpl activeGameRepository;

    @BeforeEach
    void setUp() {
        activeGameRepository = new ActiveGameRepositoryImpl();
    }

    @Test
    void findByIdShouldReturnCorrectGameAfterPut() {
        //given
        Game game = DummyData.getGameA();

        //when
        Long result = activeGameRepository.put(game);

        //then
        assertEquals(game, activeGameRepository.findById(result).get());
    }

    @Test
    void findByIdShouldReturnEmptyOptionalOnBadId() {
        //when
        var result = activeGameRepository.findById(1L);

        //then
        assertFalse(result.isPresent());
    }

}
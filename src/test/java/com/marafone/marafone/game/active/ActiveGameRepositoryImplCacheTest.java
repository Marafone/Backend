package com.marafone.marafone.game.active;
import com.marafone.marafone.DummyData;
import com.marafone.marafone.game.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ActiveGameRepositoryImplCacheTest {

    private ActiveGameRepository activeGameRepository;

    @BeforeEach
    void setUp() {
        activeGameRepository = new ActiveGameRepositoryImplCache(new ActiveGameRepositoryImpl());
    }

    @Test
    void testGetWaitingGames() {
        //should create snapshot
        activeGameRepository.getWaitingGames();

        //save game
        Game game = DummyData.getGameInLobby();
        Long result = activeGameRepository.put(game);

        //should return 0, snapshot not stale yet
        assertEquals(0, activeGameRepository.getWaitingGames().size());

        //wait for snapshot to go stale (should mock waiting time instead of waiting)
        try{
            Thread.sleep(2100);
        }catch (InterruptedException e){
            assertEquals(false, true);
        }

        //snapshot is stale -> should fetch up-to-date data
        assertEquals(1, activeGameRepository.getWaitingGames().size());
    }

}
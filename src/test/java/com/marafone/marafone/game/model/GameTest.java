package com.marafone.marafone.game.model;
import com.marafone.marafone.DummyData;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void testNewOrderAfterRoundEnd(){
        //given
        LinkedList<GamePlayer> gamePlayers = new LinkedList<>(List.of(
                DummyData.getGamePlayerRedA(), DummyData.getGamePlayerBlueA(),
                DummyData.getGamePlayerRedB(), DummyData.getGamePlayerBlueB()
        ));

        LinkedList<GamePlayer> initialGamePlayers = new LinkedList<>(List.of(
                DummyData.getGamePlayerRedB(), DummyData.getGamePlayerBlueB(),
                DummyData.getGamePlayerRedA(), DummyData.getGamePlayerBlueA()
        ));

        Game game = Game.builder()
                .rounds(new LinkedList<>(List.of(DummyData.getRoundA())))
                .playersList(gamePlayers).initialPlayersList(initialGamePlayers)
                .build();

        //when
        game.setNewOrderAfterRoundEnd();

        //then
        assertEquals(
                game.getPlayersList(),
                List.of(
                        DummyData.getGamePlayerBlueB(), DummyData.getGamePlayerRedA(),
                        DummyData.getGamePlayerBlueA(), DummyData.getGamePlayerRedB()
                )
        );
    }

}
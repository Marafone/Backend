package com.marafone.marafone.game.broadcaster;

import com.marafone.marafone.game.event.outgoing.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameBroadcaster implements EventBroadcaster{

    private final SimpMessagingTemplate template;
    private final String topic = "/topic/game/";

    @Override
    public void publishGameState(Long gameId, GameState gameState) {

    }
    @Override
    public void publishMyCardsState(Long gameId, MyCardsState myCardsState) {

    }
    @Override
    public void publishPlayersInfoState(Long gameId, PlayersInfoState playersInfoState) {

    }
    @Override
    public void publishTrumpSuitState(Long gameId, TrumpSuitState trumpSuitState) {

    }
    @Override
    public void publishTurnState(Long gameId, TurnState turnState) {

    }
    @Override
    public void publishWinnerState(Long gameId, WinnerState winnerState) {

    }
}

package com.marafone.marafone.game.broadcaster;
import com.marafone.marafone.game.event.outgoing.*;
public interface EventBroadcaster {
    void publishGameState(Long gameId, GameState gameState);
    void publishMyCardsState(Long gameId, MyCardsState myCardsState);
    void publishPlayersInfoState(Long gameId, PlayersInfoState playersInfoState);
    void publishTrumpSuitState(Long gameId, TrumpSuitState trumpSuitState);
    void publishTurnState(Long gameId, TurnState turnState);
    void publishWinnerState(Long gameId, WinnerState winnerState);
}

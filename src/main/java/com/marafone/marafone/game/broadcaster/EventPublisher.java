package com.marafone.marafone.game.broadcaster;
import com.marafone.marafone.game.event.outgoing.*;
public interface EventPublisher {
    void publishGameState(Long gameId, GameState gameState);
    void publishMyCardsState(Long gameId, MyCardsState myCardsState, String principalName);
    void publishPlayersOrderState(Long gameId, PlayersOrderState playersInfoState);
    void publishPointState(Long gameId, PointState pointState);
    void publishTeamState(Long gameId, TeamState teamState);
    void publishTrumpSuitState(Long gameId, TrumpSuitState trumpSuitState);
    void publishTurnState(Long gameId, TurnState turnState);
    void publishWinnerState(Long gameId, WinnerState winnerState);
}

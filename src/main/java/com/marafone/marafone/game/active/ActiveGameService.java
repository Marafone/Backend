package com.marafone.marafone.game.active;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.user.User;
import org.springframework.messaging.handler.annotation.DestinationVariable;

public interface ActiveGameService {
    Long createGame(CreateGameRequest createGameRequest, User user);
    Boolean joinGame(Long gameId, JoinGameRequest joinGameRequest, User user);
    void checkTimeout(@DestinationVariable Long gameId);
    void startGame(@DestinationVariable Long gameId, String principalName);
    void selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName);
    void selectSuit(Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName);
    void reconnectToGame(Long gameId, String principalName);
}

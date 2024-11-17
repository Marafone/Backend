package com.marafone.marafone.game.active;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import org.springframework.messaging.handler.annotation.DestinationVariable;

import java.security.Principal;
public interface ActiveGameService {
    Long createGame(CreateGameRequest createGameRequest, String principalName);
    Boolean joinGame(Long gameId, JoinGameRequest joinGameRequest, String principalName);
    void selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName);
    void selectSuit(Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName);
    void checkTimeout(@DestinationVariable Long gameId);
    void startGame(@DestinationVariable Long gameId, String principalName);
}

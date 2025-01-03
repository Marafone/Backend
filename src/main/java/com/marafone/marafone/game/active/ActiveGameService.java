package com.marafone.marafone.game.active;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.user.User;
import org.springframework.messaging.handler.annotation.DestinationVariable;

import java.util.List;
import java.util.Map;

public interface ActiveGameService {
    List<GameDTO> getWaitingGames();
    Long createGame(CreateGameRequest createGameRequest, User user);
    JoinGameResult joinGame(Long gameId, JoinGameRequest joinGameRequest, User user);
    void leaveGame(Long gameId, User user);
    void changeTeam(Long gameId, Team team, User user);
    void checkTimeout(@DestinationVariable Long gameId);
    void startGame(@DestinationVariable Long gameId, String principalName);
    void selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName);
    void selectSuit(Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName);
    void reconnectToGame(Long gameId, String principalName);
    boolean doesNotStartedGameAlreadyExist(String name);
}

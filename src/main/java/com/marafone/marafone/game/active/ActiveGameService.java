package com.marafone.marafone.game.active;

import com.marafone.marafone.game.dto.GameDTO;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.game.model.Call;
import com.marafone.marafone.game.model.Team;
import com.marafone.marafone.game.response.JoinGameResult;
import com.marafone.marafone.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;

import java.util.List;
import java.util.Optional;

public interface ActiveGameService {
    GameDTO getWaitingGameById(Long id);
    List<GameDTO> getWaitingGames();
    Optional<Long> getActiveGameForPlayer(String playerName);
    Long createGame(CreateGameRequest createGameRequest, User user);
    JoinGameResult joinGame(Long gameId, JoinGameRequest joinGameRequest, User user);
    void leaveGame(Long gameId, User user);
    void changeTeam(Long gameId, Team team, User user);
    AddAIResult addAI(Long gameId, Team team, User user);
    void checkTimeout(@DestinationVariable Long gameId);
    void startGame(@DestinationVariable Long gameId, String principalName);
    MakeAIMoveResult makeAIMove(Long gameId, String playerUsername);
    SelectCardResult selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName);
    void selectSuit(Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName);
    void sendCall(Long gameId, Call call);
    void reconnectToGame(Long gameId, String principalName);
    boolean doesNotStartedGameAlreadyExist(String name);
    boolean checkIfUserIsInGame(User user);
}

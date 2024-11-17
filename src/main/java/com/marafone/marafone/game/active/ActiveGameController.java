package com.marafone.marafone.game.active;

import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ActiveGameController {

    private final ActiveGameService activeGameService;

    /* Principal is the owner of the game. Should return id of the created game. */
    @PostMapping("/game/create")
    public Long createGame(@RequestBody CreateGameRequest createGameRequest, Principal principal){
        return null;
    }

    /* Should return 200 OK when user correctly joins the game and broadcast new PlayerInfoState to /topic/game/{id}*/
    @PostMapping("/game/{id}/join")
    public ResponseEntity<Void> joinGame(@PathVariable("id") Long gameId, @RequestBody JoinGameRequest joinGameRequest, Principal principal){

        return null;
    }

    /*
        Should select card. Broadcast new MyCardsState to principal. Broadcast new TurnState to everyone.
        Broadcast new GameState with 0.5s delay if round ended. Broadcast WinnerNotification with 0.5s delay and save the game to
        relational db if the game ended.
    */
    @MessageMapping("/game/{id}/card")
    public void selectCard(@DestinationVariable Long gameId, CardSelectEvent cardSelectEvent, Principal principal){

    }
    /* Should broadcast new TrumpSuitState */
    @MessageMapping("/game/{id}/suit")
    public void selectSuit(@DestinationVariable Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, Principal principal){

    }

    /*
        Frontend sends request to this endpoint after 16 seconds of not changing the state of the game,
        if last action was made more than 15 seconds ago this should make a random move (acts similar to selectCard method).
     */
    @MessageMapping("/game/{id}/timeout")
    public void checkTimeout(@DestinationVariable Long gameId){

    }

    /*
        This should start the game if lobby is full and it is the owner of the lobby that sent the request
        Broadcast GameState event, because we need to setup whole UI.
     */
    @MessageMapping("/game/{id}/start")
    public void startGame(@DestinationVariable Long gameId, Principal principal){

    }

}

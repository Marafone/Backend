package com.marafone.marafone.game.active;

import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class ActiveGameController {

    private final ActiveGameService activeGameService;

    @PostMapping("/game/create")
    @ResponseBody
    public String createGame(@RequestBody CreateGameRequest createGameRequest, Principal principal){
        Long gameId = activeGameService.createGame(createGameRequest, principal.getName());
        return String.valueOf(gameId);
    }

    @PostMapping("/game/{id}/join")
    @ResponseBody
    public ResponseEntity<Void> joinGame(@PathVariable("id") Long gameId, @RequestBody JoinGameRequest joinGameRequest, Principal principal){
        try {
            boolean joined = activeGameService.joinGame(gameId, joinGameRequest, principal.getName());
            if(joined)
                return new ResponseEntity<>(HttpStatus.OK);
            else
                return new ResponseEntity<>(HttpStatus.CONFLICT);
        }catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /*
        Should select card. Broadcast new MyCardsState to principal. Broadcast new TurnState to everyone.
        Broadcast new GameState with 0.5s delay if round ended. Broadcast WinnerNotification with 0.5s delay and save the game to
        relational db if the game ended.
    */
    @MessageMapping("/game/{id}/card")
    public void selectCard(@DestinationVariable("id") Long gameId, CardSelectEvent cardSelectEvent, Principal principal){

    }
    /* Should broadcast new TrumpSuitState */
    @MessageMapping("/game/{id}/suit")
    public void selectSuit(@DestinationVariable("id") Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, Principal principal){
        activeGameService.selectSuit(gameId, trumpSuitSelectEvent, principal.getName());
    }

    /*
        Frontend sends request to this endpoint after 16 seconds of not changing the state of the game,
        if last action was made more than 15 seconds ago this should make a random move (acts similar to selectCard method).
     */
    @MessageMapping("/game/{id}/timeout")
    public void checkTimeout(@DestinationVariable("id") Long gameId){

    }

    @MessageMapping("/game/{id}/start")
    public void startGame(@DestinationVariable("id") Long gameId, Principal principal){
        activeGameService.startGame(gameId, principal.getName());
    }

    @MessageMapping("/game/{id}/reconnect")
    public void reconnectToGame(@DestinationVariable("id") Long gameId, Principal principal){
        activeGameService.reconnectToGame(gameId, principal.getName());
    }

}

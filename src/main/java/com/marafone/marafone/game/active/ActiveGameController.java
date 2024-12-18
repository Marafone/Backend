package com.marafone.marafone.game.active;

import com.marafone.marafone.game.ended.EndedGameService;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.GameDTO;
import com.marafone.marafone.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class ActiveGameController {

    private final ActiveGameService activeGameService;

    @GetMapping("/game/public")
    @ResponseBody
    public List<Game> getPublicGames(){
        return activeGameService.getPublicGames();
    }

    // get all games to display them to user at landing page as lobbies
    // it does same thing as getPublicGames() but transforms it into GameDTO
    @GetMapping("/game/waiting")
    @ResponseBody
    public List<GameDTO> getWaitingGames() {
        return activeGameService.getWaitingGames();
    }

    @PostMapping("/game/create")
    @ResponseBody
    public String createGame(@RequestBody CreateGameRequest createGameRequest, @AuthenticationPrincipal User user){
        Long gameId = activeGameService.createGame(createGameRequest, user);
        return String.valueOf(gameId);
    }

    @PostMapping("/game/{id}/join")
    @ResponseBody
    public ResponseEntity<Void> joinGame(@PathVariable("id") Long gameId, @RequestBody JoinGameRequest joinGameRequest, @AuthenticationPrincipal User user){
        try {
            boolean joined = activeGameService.joinGame(gameId, joinGameRequest, user);
            if(joined)
                return new ResponseEntity<>(HttpStatus.OK);
            else
                return new ResponseEntity<>(HttpStatus.CONFLICT);
        }catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @MessageMapping("/game/{id}/card")
    public void selectCard(@DestinationVariable("id") Long gameId, CardSelectEvent cardSelectEvent, Principal principal){
        activeGameService.selectCard(gameId, cardSelectEvent, principal.getName());
    }

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

    //WILL REMOVE IT LATER only for debugging/testing hibernate
    private final EndedGameService endedGameService;
    private final ActiveGameRepository activeGameRepository;

    @MessageMapping("/game/{id}/save")
    public void save(@DestinationVariable("id") Long gameId){
        endedGameService.saveEndedGame(activeGameRepository.findById(gameId).get());
    }


}

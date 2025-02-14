package com.marafone.marafone.game.active;

import com.marafone.marafone.game.ended.EndedGameService;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.game.response.GameActionResponse;
import com.marafone.marafone.game.response.JoinGameResponse;
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
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ActiveGameController {

    private final ActiveGameService activeGameService;

    @GetMapping("/game/waiting")
    @ResponseBody
    public List<GameDTO> getWaitingGames() {
        return activeGameService.getWaitingGames();
    }

    @PostMapping("/game/create")
    @ResponseBody
    public synchronized ResponseEntity<String> createGame(@RequestBody CreateGameRequest createGameRequest, @AuthenticationPrincipal User user){
        if (activeGameService.doesNotStartedGameAlreadyExist(createGameRequest.getGameName()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("GAME_NAME_TAKEN");

        Long gameId = activeGameService.createGame(createGameRequest, user);
        return ResponseEntity.ok(String.valueOf(gameId));
    }

    @PostMapping("/game/{id}/join")
    @ResponseBody
    public ResponseEntity<JoinGameResponse> joinGame(@PathVariable("id") Long gameId, @RequestBody JoinGameRequest joinGameRequest, @AuthenticationPrincipal User user){
        JoinGameResult result = activeGameService.joinGame(gameId, joinGameRequest, user);
        JoinGameResponse joinGameResponse = new JoinGameResponse(result, result.getMessage());
        if (result == JoinGameResult.SUCCESS)
            return ResponseEntity.ok().body(joinGameResponse);
        else
            return ResponseEntity.badRequest().body(joinGameResponse);
    }

    @PostMapping("/game/{id}/leave")
    @ResponseBody
    public void leaveGame(@PathVariable("id") Long gameId, @AuthenticationPrincipal User user) {
        activeGameService.leaveGame(gameId, user);
    }

    @PostMapping("/game/{id}/team/change")
    @ResponseBody
    public void changeTeam(@PathVariable("id") Long gameId, @RequestBody String teamName, @AuthenticationPrincipal User user) {
        activeGameService.changeTeam(gameId, Team.valueOf(teamName), user);
    }

    @PostMapping("/game/{id}/add-ai")
    @ResponseBody
    public ResponseEntity<GameActionResponse> addAI(@PathVariable("id") Long gameId, @RequestBody String teamName, @AuthenticationPrincipal User user) {
        AddAIResult result = activeGameService.addAI(gameId, Team.valueOf(teamName), user);
        if(result == AddAIResult.SUCCESS){
            return ResponseEntity.ok().body( new GameActionResponse(true, result.getMessage()));
        }
        else
            return ResponseEntity.badRequest().body(new GameActionResponse(false, result.getMessage()));
    }

    @PostMapping("/game/{id}/ai-move")
    @ResponseBody
    public ResponseEntity<GameActionResponse> makeAIMove(@PathVariable("id") Long gameId, @RequestBody String playerUsername) {
        MakeAIMoveResult result = activeGameService.makeAIMove(gameId, playerUsername);
        if(result == MakeAIMoveResult.SUCCESS){
            return ResponseEntity.ok().body(new GameActionResponse(true, result.getMessage()));
        }
        else
            return ResponseEntity.badRequest().body(new GameActionResponse(false, result.getMessage()));
    }

    @MessageMapping("/game/{id}/card")
    public ResponseEntity<GameActionResponse> selectCard(@DestinationVariable("id") Long gameId, CardSelectEvent cardSelectEvent, Principal principal){
        SelectCardResult result = activeGameService.selectCard(gameId, cardSelectEvent, principal.getName());
        if(result == SelectCardResult.SUCCESS){
            return ResponseEntity.ok().body(new GameActionResponse(true, result.getMessage()));
        }
        else
            return ResponseEntity.badRequest().body(new GameActionResponse(false, result.getMessage()));
    }

    @MessageMapping("/game/{id}/suit")
    public void selectSuit(@DestinationVariable("id") Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, Principal principal){
        activeGameService.selectSuit(gameId, trumpSuitSelectEvent, principal.getName());
    }

    @MessageMapping("/game/{id}/call")
    public void sendCall(@DestinationVariable("id") Long gameId, Call call) {
        activeGameService.sendCall(gameId, call);
    }

    @MessageMapping("/game/{id}/timeout")
    public void checkTimeout(@DestinationVariable("id") Long gameId){
        activeGameService.checkTimeout(gameId);
    }

    @MessageMapping("/game/{id}/start")
    public void startGame(@DestinationVariable("id") Long gameId, Principal principal){
        activeGameService.startGame(gameId, principal.getName());
    }

    @MessageMapping("/game/{id}/reconnect")
    public void reconnectToGame(@DestinationVariable("id") Long gameId, Principal principal){
        activeGameService.reconnectToGame(gameId, principal.getName());
    }

    //TODO WILL REMOVE IT LATER only for debugging/testing hibernate
    private final EndedGameService endedGameService;
    private final ActiveGameRepository activeGameRepository;

    @MessageMapping("/game/{id}/save")
    public void save(@DestinationVariable("id") Long gameId){
        endedGameService.saveEndedGame(activeGameRepository.findById(gameId).get());
    }


}

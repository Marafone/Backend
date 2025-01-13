package com.marafone.marafone.game.ended;

import com.marafone.marafone.game.model.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class EndedGameController {

    private final EndedGameService endedGameService;

    @GetMapping("/game/{id}/ended")
    public ResponseEntity<Game> getEndedGame(@PathVariable("id") Long id){
        Optional<Game> endedGameById = endedGameService.getEndedGameById(id);
        return endedGameById.map(game -> new ResponseEntity<>(game, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/games/ended/player")
    public ResponseEntity<List<Game>> getPlayerEndedGames(Principal principal) {
        List<Game> gamesList = endedGameService.getPlayerEndedGames(principal.getName());
        return gamesList == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(gamesList);
    }
}

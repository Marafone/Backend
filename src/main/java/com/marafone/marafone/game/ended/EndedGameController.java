package com.marafone.marafone.game.ended;

import com.marafone.marafone.game.model.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EndedGameController {

    private final EndedGameService endedGameService;

    @GetMapping("/game/{id}/ended")
    public Game getEndedGame(@PathVariable("id") Long id){
        return null;
    }
}

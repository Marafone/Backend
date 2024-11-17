package com.marafone.marafone.game.ended;
import com.marafone.marafone.game.model.Game;

import java.util.Optional;
public interface EndedGameService {
    Optional<Game> getEndedGameById(Long id);
    Game saveEndedGame(Game game);
}

package com.marafone.marafone.game.ended;

import com.marafone.marafone.game.model.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EndedGameServiceImpl implements EndedGameService{

    private final EndedGameRepository endedGameRepository;

    @Override
    public Optional<Game> getEndedGameById(Long id) {
        return Optional.empty();
    }

    @Override
    public Game saveEndedGame(Game game) {
        return null;
    }
}

package com.marafone.marafone.game.ended;

import com.marafone.marafone.game.model.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EndedGameServiceImpl implements EndedGameService{

    private final EndedGameRepository endedGameRepository;

    @Override
    public Optional<Game> getEndedGameById(Long id) {
        return endedGameRepository.findById(id);
    }

    @Override
    public Game saveEndedGame(Game game) {
        return endedGameRepository.save(game);
    }

    @Override
    public List<Game> getPlayerEndedGames(String playerName) {
        return endedGameRepository.findAllByPlayerName(playerName);
    }
}

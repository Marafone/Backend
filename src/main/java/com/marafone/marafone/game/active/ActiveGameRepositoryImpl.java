package com.marafone.marafone.game.active;

import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.Team;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveGameRepositoryImpl implements ActiveGameRepository{

    ConcurrentHashMap<Long, Game> activeGames = new ConcurrentHashMap<>();

    @Override
    public Optional<Game> findById(Long id) {
        return Optional.ofNullable(activeGames.get(id));
    }

    @Override
    public Long put(Game game) {
        var uuid = UUID.randomUUID().getMostSignificantBits();

        while(true){
            game = activeGames.putIfAbsent(uuid, game);

            if(game == null)
                return uuid;

            uuid = UUID.randomUUID().getMostSignificantBits();
        }
    }

    @Override
    public List<Game> getPublicGames() {
        return activeGames.values().stream()
                .filter(game -> !game.hasStarted() && game.isPublic() && game.anyTeamNotFull())
                .toList();
    }
}

package com.marafone.marafone.game.active;

import com.marafone.marafone.game.model.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class ActiveGameRepositoryImplCache implements ActiveGameRepository{

    private final ActiveGameRepositoryImpl activeGameRepositoryImpl;
    LocalDateTime LastSnapshotDateTime;
    List<Game> LastSnapshotData;

    private final int SNAPSHOT_STALE_TIME = 2;

    @Override
    public Optional<Game> findById(Long id) {
        return activeGameRepositoryImpl.findById(id);
    }
    @Override
    public void removeById(Long id) {
        activeGameRepositoryImpl.removeById(id);
    }
    @Override
    public Long put(Game game) {
        return activeGameRepositoryImpl.put(game);
    }
    @Override
    public List<Game> getWaitingGames() {
        if(LastSnapshotDateTime == null || LocalDateTime.now().isAfter(LastSnapshotDateTime.plusSeconds(SNAPSHOT_STALE_TIME))){
            LastSnapshotDateTime = LocalDateTime.now();
            LastSnapshotData = activeGameRepositoryImpl.getWaitingGames();
        }

        return LastSnapshotData;
    }
}

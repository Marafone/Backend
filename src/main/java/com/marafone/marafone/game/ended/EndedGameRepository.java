package com.marafone.marafone.game.ended;

import com.marafone.marafone.game.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndedGameRepository extends JpaRepository<Game, Long> {
}

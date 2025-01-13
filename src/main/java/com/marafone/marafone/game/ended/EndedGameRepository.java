package com.marafone.marafone.game.ended;

import com.marafone.marafone.game.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EndedGameRepository extends JpaRepository<Game, Long> {
    @Query("SELECT g FROM Game g JOIN g.playersList p WHERE p.user.username = :username")
    List<Game> findAllByPlayerName(@Param("username") String username);
}

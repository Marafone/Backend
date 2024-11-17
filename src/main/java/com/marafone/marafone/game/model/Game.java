package com.marafone.marafone.game.model;

import com.marafone.marafone.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Game {
    @Id
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private Team winnerTeam;
    private List<GamePlayer> playersList;
    private List<Round> rounds;
    private GameType gameType;
    private User owner;
    @Transient
    private GamePlayer currentPlayerTurn;
}

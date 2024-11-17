package com.marafone.marafone.game.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Action {
    @Id
    private Long id;
    private GamePlayer player;
    private Round round;
    private LocalDateTime timestamp;
}

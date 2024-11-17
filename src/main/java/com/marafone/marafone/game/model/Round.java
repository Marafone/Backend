package com.marafone.marafone.game.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class Round {
    @Id
    private Long id;
    private List<Action> actions;
    private Suit trumpSuit;
}

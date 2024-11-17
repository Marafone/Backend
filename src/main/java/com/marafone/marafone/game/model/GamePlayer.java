package com.marafone.marafone.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marafone.marafone.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.util.List;

@Entity
public class GamePlayer {
    @Id
    private Long id;
    private User user;
    private Team team;
    private Integer points;
    @Transient
    @JsonIgnore
    private List<Card> ownedCards;
}

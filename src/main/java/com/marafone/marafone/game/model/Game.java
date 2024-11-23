package com.marafone.marafone.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ListIterator;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private Team winnerTeam;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn
    private List<GamePlayer> playersList;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn
    private List<Round> rounds;
    @Enumerated(EnumType.STRING)
    private GameType gameType;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn
    private GamePlayer owner;
    @Transient
    private ListIterator<GamePlayer> currentPlayerTurn;
}

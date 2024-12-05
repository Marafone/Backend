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
    private String name;
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
    private String joinGameCode;
    @Transient
    private ListIterator<GamePlayer> currentPlayerTurn;

    public boolean hasStarted(){
        return startedAt != null;
    }

    public boolean checkCode(String joinGameCode){
        return this.joinGameCode == null || this.joinGameCode.equals(joinGameCode);
    }

    public boolean teamIsFull(Team joiningTeam){
        return playersList.stream().filter(x -> x.getTeam().equals(joiningTeam)).count() == 2;
    }

    public boolean playerAlreadyJoined(String principalName){
        return playersList.stream().map(player -> player.getUser().getUsername()).anyMatch(username -> username.equals(principalName));
    }
}

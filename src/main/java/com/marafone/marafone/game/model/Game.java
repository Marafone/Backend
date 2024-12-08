package com.marafone.marafone.game.model;

import com.marafone.marafone.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedList;
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
    @ManyToOne
    @JoinColumn
    private User owner;
    private String joinGameCode;
    @Transient
    private ListIterator<GamePlayer> currentPlayer;
    @Transient
    private List<GamePlayer> initialPlayersList;

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

    public boolean turnHasEnded(){
        return !currentPlayer.hasNext();
    }

    public boolean roundHasEnded(){
        return playersList.getFirst().getOwnedCards().isEmpty();
    }

    public boolean setWinnersIfPossible(){
        int blueTeamPoints = 0;
        int redTeamPoints = 0;

        for(var gamePlayer: playersList){
            if(gamePlayer.getTeam() == Team.RED)
                redTeamPoints += gamePlayer.getPoints();
            else
                blueTeamPoints += gamePlayer.getPoints();
        }

        if(blueTeamPoints != redTeamPoints && (blueTeamPoints >= 21 || redTeamPoints >= 21)){
            if(blueTeamPoints > redTeamPoints){
                winnerTeam = Team.BLUE;
            }else{
                winnerTeam = Team.RED;
            }
            return true;
        }

        return false;
    }

}

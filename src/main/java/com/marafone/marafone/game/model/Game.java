package com.marafone.marafone.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marafone.marafone.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

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
    @Getter
    private String joinGameCode;
    @Transient
    private ListIterator<GamePlayer> currentPlayer;
    @Transient
    private List<GamePlayer> initialPlayersList;
    @Transient
    private Suit leadingSuit;

    @Transient
    @JsonIgnore
    @Builder.Default
    public int pointsToWinGame = 63;

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
        for(var gamePlayer: playersList){
            if(!gamePlayer.getOwnedCards().isEmpty())
                return false;
        }
        return true;
    }

    public boolean hasEnded(){
        return winnerTeam != null;
    }

    public boolean isPublic(){
        return joinGameCode == null || joinGameCode.isEmpty();
    }

    public boolean anyTeamNotFull(){
        return playersList.size() < 4;
    }

    public boolean isEnded() {return winnerTeam != null;}

    public boolean isSettingWinnersPossible() {
        int redTeamPoints = getTeamPoints(Team.RED);
        int blueTeamPoints = getTeamPoints(Team.BLUE);

        return (redTeamPoints != blueTeamPoints) && ((redTeamPoints >= pointsToWinGame) || (blueTeamPoints >= pointsToWinGame));
    }

    public void setWinners() {
        int redTeamPoints = getTeamPoints(Team.RED);
        int blueTeamPoints = getTeamPoints(Team.BLUE);

        if (blueTeamPoints > redTeamPoints){
            winnerTeam = Team.BLUE;
        } else {
            winnerTeam = Team.RED;
        }
    }

    public void addRound(){
        rounds.add(Round.builder().actions(new LinkedList<>()).build());
    }

    public GamePlayer findGamePlayerByUsername(String username){
        return playersList.stream()
                .filter(player -> player.getUser().getUsername().equals(username)).findFirst()
                .orElse(null);
    }

    public void setNewOrderAfterTurnEnd(GamePlayer winner){
        List<GamePlayer> newOrder = new ArrayList<>();
        for(var gamePlayer: playersList){
            if(gamePlayer.equals(winner) || !newOrder.isEmpty()){
                newOrder.addLast(gamePlayer);
            }
        }
        for(var gamePlayer: playersList){
            if(newOrder.size() == playersList.size())
                break;

            newOrder.addLast(gamePlayer);
        }
        playersList = newOrder;
        currentPlayer = newOrder.listIterator();
    }

    public void setNewOrderAfterRoundEnd(){
        int startingPlayerIndex = rounds.size() % playersList.size();

        List<GamePlayer> newOrder = new ArrayList<>();
        for(int i = startingPlayerIndex; i < initialPlayersList.size(); i++){
            newOrder.addLast(initialPlayersList.get(i));
        }
        for(int i = 0; i < startingPlayerIndex; i++){
            newOrder.addLast(initialPlayersList.get(i));
        }
        playersList = newOrder;
        currentPlayer = newOrder.listIterator();
    }

    public int getPlayersAmount() {
        return playersList.size();
    }

    public GamePlayer getCurrentPlayerWithoutIterating(){
        if(!currentPlayer.hasNext())
            return null;

        GamePlayer gamePlayer = currentPlayer.next();
        currentPlayer.previous();
        return gamePlayer;
    }

    public List<GamePlayer> getGamePlayersFromTeam(Team team) {
        return playersList.stream()
                .filter(gamePlayer ->  gamePlayer.getTeam() == team)
                .toList();
    }

    public int getTeamPoints(Team team) {
        return getGamePlayersFromTeam(team).stream()
                .map(GamePlayer::getPoints)
                .reduce(0, Integer::sum);
    }

    public GamePlayer findTopScorerInTeam(Team team) {
        var gamePlayersTeam = getGamePlayersFromTeam(team);
        GamePlayer topScorer = null;

        for (var gamePlayer: gamePlayersTeam) {
            if (topScorer == null || gamePlayer.getPoints() > topScorer.getPoints())
                topScorer = gamePlayer;
        }

        return topScorer;
    }

    public int getCurrentPlayerIndex() {
        if (currentPlayer == null) {
            throw new IllegalStateException("currentPlayer iterator is not initialized");
        }
        return currentPlayer.nextIndex();
    }

    public int getRoundNumber() {
        return rounds.size();
    }

    public void setLeadingSuitIfUnset(Suit suit) {
        if (leadingSuit == null)
            leadingSuit = suit;
    }

}

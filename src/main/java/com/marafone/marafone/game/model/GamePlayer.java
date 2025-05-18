package com.marafone.marafone.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marafone.marafone.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GamePlayer implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    @JoinColumn
    @ManyToOne
    private User user;
    @Enumerated(EnumType.STRING)
    private Team team;
    private Integer points;
    @Transient
    @JsonIgnore
    private List<Card> ownedCards;

    @Override
    public int hashCode() {
        return Objects.hash(id); // id should be a unique, immutable identifier
    }

    @Override
    public boolean equals(Object o){
        if (o == this)
            return true;

        if (!(o instanceof GamePlayer other))
            return false;

        if(this.getUser() == null || other.getUser() == null)
            return false;

        return other.getUser().getUsername().equals(this.getUser().getUsername());
    }

    public boolean hasFourOfCoins(){
        return ownedCards.stream().anyMatch(card -> card.getSuit() == Suit.COINS && card.getRank() == CardRank.FOUR);
    }

    public boolean hasCard(Card card){
        return ownedCards.stream().anyMatch(iterCard -> iterCard.equals(card));
    }

    public void addPoints(int points){
        this.points += points;
    }

    public void subtractPoints(int points) { this.points -= points; }

    public void addBonusPoint(){
        addPoints(3);
    }

    public boolean hasCardOfSuit(Suit wantedSuit){
        return ownedCards.stream().map(Card::getSuit).anyMatch(suit -> suit == wantedSuit);
    }

    public void removeCard(Card toBeRemoved){
        ownedCards.removeIf(card -> card.equals(toBeRemoved));
    }
}

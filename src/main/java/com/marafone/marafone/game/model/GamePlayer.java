package com.marafone.marafone.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marafone.marafone.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GamePlayer {
    @Id
    @GeneratedValue
    private Long id;
    @JoinColumn
    @ManyToOne(cascade = CascadeType.ALL)
    private User user;
    @Enumerated(EnumType.STRING)
    private Team team;
    private Integer points;
    @Transient
    @JsonIgnore
    private List<Card> ownedCards;

    public boolean hasFourOfCoins(){
        return ownedCards.stream().anyMatch(card -> card.getSuit() == Suit.COINS && card.getRank() == CardRank.FOUR);
    }
}

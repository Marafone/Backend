package com.marafone.marafone.game.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Card implements Comparable<Card>{

    @Id
    private Long id;
    private CardRank rank;
    private Suit suit;

    @Override
    public int compareTo(Card o) {
        return 0;
    }
}

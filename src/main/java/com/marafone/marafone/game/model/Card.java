package com.marafone.marafone.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Card implements Comparable<Card>{

    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private CardRank rank;
    @Enumerated(EnumType.STRING)
    private Suit suit;

    @Override
    public int compareTo(Card o) {
        return 0;
    }
}

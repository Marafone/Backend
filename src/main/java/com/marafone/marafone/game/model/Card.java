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
public class Card{

    @Id
    private Long id;
    @Enumerated(EnumType.STRING)
    private CardRank rank;
    @Enumerated(EnumType.STRING)
    private Suit suit;

    @Override
    public boolean equals(Object obj){
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Card card)) {
            return false;
        }

        return this.rank == card.rank && this.suit == card.suit;
    }

    @Override
    public int hashCode() {
        int a;
        if(suit == Suit.SWORDS)
            a = 2;
        else if(suit == Suit.COINS)
            a = 3;
        else if(suit == Suit.CLUBS)
            a = 4;
        else
            a = 5;

        return 11 * rank.getPoints() + a;
    }
}

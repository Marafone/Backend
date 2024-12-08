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
}

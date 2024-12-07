package com.marafone.marafone.game.config;

import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.CardRank;
import com.marafone.marafone.game.model.Suit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.List;

@Configuration
public class CardConfig {

    @Bean
    public List<Card> allCards() {
        List<Card> allCards = new LinkedList<>();

        allCards.add(new Card(1L, CardRank.THREE, Suit.SWORDS));
        allCards.add(new Card(2L, CardRank.TWO, Suit.SWORDS));
        allCards.add(new Card(3L, CardRank.A, Suit.SWORDS));
        allCards.add(new Card(4L, CardRank.K, Suit.SWORDS));
        allCards.add(new Card(5L, CardRank.C, Suit.SWORDS));
        allCards.add(new Card(6L, CardRank.J, Suit.SWORDS));
        allCards.add(new Card(7L, CardRank.SEVEN, Suit.SWORDS));
        allCards.add(new Card(8L, CardRank.SIX, Suit.SWORDS));
        allCards.add(new Card(9L, CardRank.FIVE, Suit.SWORDS));
        allCards.add(new Card(10L, CardRank.FOUR, Suit.SWORDS));

        allCards.add(new Card(11L, CardRank.THREE, Suit.CUPS));
        allCards.add(new Card(12L, CardRank.TWO, Suit.CUPS));
        allCards.add(new Card(13L, CardRank.A, Suit.CUPS));
        allCards.add(new Card(14L, CardRank.K, Suit.CUPS));
        allCards.add(new Card(15L, CardRank.C, Suit.CUPS));
        allCards.add(new Card(16L, CardRank.J, Suit.CUPS));
        allCards.add(new Card(17L, CardRank.SEVEN, Suit.CUPS));
        allCards.add(new Card(18L, CardRank.SIX, Suit.CUPS));
        allCards.add(new Card(19L, CardRank.FIVE, Suit.CUPS));
        allCards.add(new Card(20L, CardRank.FOUR, Suit.CUPS));

        allCards.add(new Card(21L, CardRank.THREE, Suit.COINS));
        allCards.add(new Card(22L, CardRank.TWO, Suit.COINS));
        allCards.add(new Card(23L, CardRank.A, Suit.COINS));
        allCards.add(new Card(24L, CardRank.K, Suit.COINS));
        allCards.add(new Card(25L, CardRank.C, Suit.COINS));
        allCards.add(new Card(26L, CardRank.J, Suit.COINS));
        allCards.add(new Card(27L, CardRank.SEVEN, Suit.COINS));
        allCards.add(new Card(28L, CardRank.SIX, Suit.COINS));
        allCards.add(new Card(29L, CardRank.FIVE, Suit.COINS));
        allCards.add(new Card(30L, CardRank.FOUR, Suit.COINS));

        allCards.add(new Card(31L, CardRank.THREE, Suit.CLUBS));
        allCards.add(new Card(32L, CardRank.TWO, Suit.CLUBS));
        allCards.add(new Card(33L, CardRank.A, Suit.CLUBS));
        allCards.add(new Card(34L, CardRank.K, Suit.CLUBS));
        allCards.add(new Card(35L, CardRank.C, Suit.CLUBS));
        allCards.add(new Card(36L, CardRank.J, Suit.CLUBS));
        allCards.add(new Card(37L, CardRank.SEVEN, Suit.CLUBS));
        allCards.add(new Card(38L, CardRank.SIX, Suit.CLUBS));
        allCards.add(new Card(39L, CardRank.FIVE, Suit.CLUBS));
        allCards.add(new Card(40L, CardRank.FOUR, Suit.CLUBS));

        return allCards;
    }

}

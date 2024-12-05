package com.marafone.marafone;

import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class DummyData {

    public static User getUserA(){
        return User.builder()
                .id(1L)
                .username("John")
                .email("john@gmail.com")
                .password("123")
                .build();
    }

    public static User getUserB(){
        return User.builder()
                .id(2L)
                .username("Maria")
                .email("Cariah")
                .password("qwerty")
                .build();
    }

    public static Card getCardA(){
        return new Card(1L, CardRank.FIVE, Suit.COINS);
    }

    public static GamePlayer getGamePlayerA(){
        return GamePlayer.builder()
                .id(1L)
                .user(getUserA())
                .team(Team.RED)
                .points(17)
                .ownedCards(
                        new ArrayList<>(
                            Arrays.asList(
                                getCardA(),
                                new Card(2L, CardRank.C, Suit.CUPS),
                                new Card(3L, CardRank.A, Suit.CLUBS)
                            )
                        )
                )
                .build();
    }

    public static GamePlayer getGamePlayerB(){
        return GamePlayer.builder()
                .id(1L)
                .user(getUserB())
                .team(Team.BLUE)
                .points(9)
                .ownedCards(
                        new ArrayList<>(
                                Arrays.asList(
                                        getCardA(),
                                        new Card(2L, CardRank.FIVE, Suit.CUPS),
                                        new Card(3L, CardRank.J, Suit.SWORDS)
                                )
                        )
                )
                .build();
    }

    public static Action getActionA(){
        return Action.builder()
                .id(1L)
                .player(getGamePlayerA())
                .card(getCardA())
                .timestamp(LocalDateTime.of(2024, 2, 27, 13, 2))
                .build();
    }

    public static Round getRoundA(){
        return Round.builder()
                .actions(new ArrayList<>(Arrays.asList(getActionA())))
                .trumpSuit(Suit.SWORDS)
                .build();
    }

    public static Game getGameA(){
        return Game.builder()
                .createdAt(LocalDateTime.of(2024, 2, 27, 13, 2))
                .startedAt(LocalDateTime.of(2024, 2, 27, 13, 3))
                .playersList(new ArrayList<>(Arrays.asList(getGamePlayerA())))
                .rounds(new ArrayList<>(Arrays.asList(getRoundA())))
                .gameType(GameType.MARAFFA)
                .owner(getGamePlayerA())
                .currentPlayerTurn(new ArrayList<>(Arrays.asList(getGamePlayerA())).listIterator())
                .build();
    }

    public static CreateGameRequest getCreateGameRequestA(){
        return new CreateGameRequest(GameType.MARAFFA, "123");
    }

    public static JoinGameRequest getJoinGameRequestA(){
        return new JoinGameRequest(Team.BLUE, null);
    }

}

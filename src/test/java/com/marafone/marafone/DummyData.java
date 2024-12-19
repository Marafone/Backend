package com.marafone.marafone;

import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

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

    public static User getUserC(){
        return User.builder()
                .id(3L)
                .username("Johny")
                .email("Wick")
                .password("qwerty!!!")
                .build();
    }

    public static User getUserD(){
        return User.builder()
                .id(4L)
                .username("Will")
                .email("Sick")
                .password("sosafe")
                .build();
    }

    public static Card getCardA(){
        return new Card(1L, CardRank.FIVE, Suit.COINS);
    }

    public static GamePlayer getGamePlayerRedA(){
        return GamePlayer.builder()
                .id(1L)
                .user(getUserA())
                .team(Team.RED)
                .points(17)
                .ownedCards(
                        new ArrayList<>(
                            Arrays.asList(
                                getCardA(),
                                new Card(1L, CardRank.A, Suit.CUPS),
                                new Card(2L, CardRank.C, Suit.CUPS)
                            )
                        )
                )
                .build();
    }

    public static GamePlayer getGamePlayerRedB(){
        return GamePlayer.builder()
                .id(1L)
                .user(getUserB())
                .team(Team.RED)
                .points(9)
                .ownedCards(
                        new ArrayList<>(
                                Arrays.asList(
                                        getCardA(),
                                        new Card(3L, CardRank.A, Suit.CLUBS),
                                        new Card(4L, CardRank.J, Suit.CLUBS)
                                )
                        )
                )
                .build();
    }

    public static GamePlayer getGamePlayerBlueA(){
        return GamePlayer.builder()
                .id(3L)
                .user(getUserC())
                .team(Team.BLUE)
                .points(11)
                .ownedCards(
                        new ArrayList<>(
                                Arrays.asList(
                                        getCardA(),
                                        new Card(5L, CardRank.FIVE, Suit.COINS),
                                        new Card(6L, CardRank.J, Suit.COINS)
                                )
                        )
                )
                .build();
    }

    public static GamePlayer getGamePlayerBlueB(){
        return GamePlayer.builder()
                .id(4L)
                .user(getUserD())
                .team(Team.BLUE)
                .points(6)
                .ownedCards(
                        new ArrayList<>(
                                Arrays.asList(
                                        getCardA(),
                                        new Card(7L, CardRank.FIVE, Suit.SWORDS),
                                        new Card(8L, CardRank.J, Suit.SWORDS)
                                )
                        )
                )
                .build();
    }

    public static Action getActionA(){
        return Action.builder()
                .id(1L)
                .player(getGamePlayerRedA())
                .card(getCardA())
                .timestamp(LocalDateTime.of(2024, 2, 27, 13, 2))
                .build();
    }

    public static Round getRoundA(){
        return Round.builder()
                .actions(new LinkedList<>(Arrays.asList(getActionA())))
                .trumpSuit(Suit.SWORDS)
                .build();
    }

    public static Game getGameInProgress(){
        return Game.builder()
                .createdAt(LocalDateTime.of(2024, 2, 27, 13, 2))
                .startedAt(LocalDateTime.of(2024, 2, 27, 13, 3))
                .playersList(new ArrayList<>(Arrays.asList(getGamePlayerRedA())))
                .rounds(new LinkedList<>(Arrays.asList(getRoundA())))
                .gameType(GameType.MARAFFA)
                .owner(getUserA())
                .currentPlayer(new ArrayList<>(Arrays.asList(getGamePlayerRedA())).listIterator())
                .build();
    }

    public static Game getGameInLobby(){
        return Game.builder()
                .createdAt(LocalDateTime.of(2024, 2, 27, 13, 2))
                .playersList(new ArrayList<>(Arrays.asList(getGamePlayerRedA())))
                .rounds(new LinkedList<>())
                .gameType(GameType.MARAFFA)
                .owner(getUserA())
                .build();
    }

    public static CreateGameRequest getCreateGameRequestA(){
        return new CreateGameRequest("name", GameType.MARAFFA, "123");
    }

    public static JoinGameRequest getJoinGameRequestA(){
        return new JoinGameRequest(Team.BLUE, null);
    }

}

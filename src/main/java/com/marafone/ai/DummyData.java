package com.marafone.ai;

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
                .username("AI_John")
                .email("john@gmail.com")
                .password("123")
                .build();
    }

    public static User getUserB(){
        return User.builder()
                .id(2L)
                .username("AI_Maria")
                .email("Cariah")
                .password("qwerty")
                .build();
    }

    public static User getUserC(){
        return User.builder()
                .id(3L)
                .username("AI_Steve")
                .email("Wick")
                .password("qwerty!!!")
                .build();
    }

    public static User getUserD(){
        return User.builder()
                .id(4L)
                .username("AI_Will")
                .email("Sick")
                .password("sosafe")
                .build();
    }
}

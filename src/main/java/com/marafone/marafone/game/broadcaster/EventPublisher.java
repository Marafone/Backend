package com.marafone.marafone.game.broadcaster;

import com.marafone.marafone.game.event.outgoing.OutEvent;

import java.util.List;

public interface EventPublisher {
    default void publishToLobby(Long gameId, OutEvent outEvent){
        publishToLobby(gameId, List.of(outEvent));
    }
    void publishToLobby(Long gameId, List<OutEvent> outEvents);

    default void publishToPlayerInTheLobby(Long gameId, String username, OutEvent outEvent){
        publishToPlayerInTheLobby(gameId, username, List.of(outEvent));
    }
    void publishToPlayerInTheLobby(Long gameId, String username, List<OutEvent> outEvents);
}

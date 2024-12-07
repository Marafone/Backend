package com.marafone.marafone.game.broadcaster;

import com.marafone.marafone.game.event.outgoing.OutEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher{

    private final SimpMessagingTemplate template;
    private final String topic = "/topic/game/";
    private final String userQueue = "/queue/game";

    @Override
    public void publishToLobby(Long gameId, List<OutEvent> outEvents) {
        template.convertAndSend(topic + gameId, outEvents);
    }

    @Override
    public void publishToPlayerInTheLobby(Long gameId, String username, List<OutEvent> outEvents) {
        template.convertAndSendToUser(username, userQueue, outEvents);
    }
}

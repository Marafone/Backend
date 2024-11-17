package com.marafone.marafone.game.active;

import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActiveGameServiceImpl implements ActiveGameService{

    private final ActiveGameRepository activeGameRepository;

    @Override
    public Long createGame(CreateGameRequest createGameRequest, String principalName) {
        return null;
    }
    @Override
    public Boolean joinGame(Long gameId, JoinGameRequest joinGameRequest, String principalName) {
        return null;
    }
    @Override
    public void selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName) {

    }
    @Override
    public void selectSuit(Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName) {

    }
    @Override
    public void checkTimeout(Long gameId) {

    }
    @Override
    public void startGame(Long gameId, String principalName) {

    }
}

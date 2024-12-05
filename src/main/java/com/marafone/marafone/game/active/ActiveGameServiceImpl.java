package com.marafone.marafone.game.active;

import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.model.Team;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActiveGameServiceImpl implements ActiveGameService{

    private final ActiveGameRepository activeGameRepository;
    private final EventPublisher eventPublisher;
    private final UserRepository userRepository;

    @Override
    public Long createGame(CreateGameRequest createGameRequest, String principalName) {
        GamePlayer gamePlayer = GamePlayer.builder()
                .user(userRepository.findByUsername(principalName).get())
                .team(Team.RED)
                .points(0)
                .build();

        Game game = Game.builder()
                .createdAt(LocalDateTime.now())
                .playersList(new LinkedList<>())
                .rounds(new LinkedList<>())
                .gameType(createGameRequest.getGameType())
                .owner(gamePlayer)
                .build();

        Long id = activeGameRepository.put(game);

        game.setName(String.valueOf(id));

        return id;
    }
    /*
        Should load game from repo, try to add player, if added player then emit events e.g. PlayersOrderState
        if could not add player (e.g. lobby was full or game was already started) then should not emit any events and just ignore the request
     */
    @Override
    public Boolean joinGame(Long gameId, JoinGameRequest joinGameRequest, String principalName) {
        return null;
    }
    /*
        Check if last action was made more than 16 sec ago and if yes then should select random card (similiar logic to selectCard)
        but with random arguments and correct principalName.
     */
    @Override
    public void checkTimeout(Long gameId) {

    }
    @Override
    public void startGame(Long gameId, String principalName) {

    }
    /*
        Should get game from repo, try to select a card (selecting a card is a write operation so needs to be in synchronized block),
        then after that if the card was selected (player had the card etc.) we do a series of checks and emit events accordingly.
        For example we need to check if the trick ended or if the round ended or if the game ended and emit proper events.
    */
    @Override
    public void selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName) {

    }
    @Override
    public void selectSuit(Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName) {

    }
}

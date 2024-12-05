package com.marafone.marafone.game.active;

import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.event.outgoing.PlayersOrderState;
import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.model.Team;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;

@Service
@RequiredArgsConstructor
public class ActiveGameServiceImpl implements ActiveGameService{

    private final ActiveGameRepository activeGameRepository;
    private final EventPublisher eventPublisher;
    private final UserRepository userRepository;

    private GamePlayer createGamePlayer(String principalName, Team team){
        return GamePlayer.builder()
                .user(
                        //userRepository.findByUsername(principalName).get()
                        new User(1L, "user", "user@gmail.com", "12312")//mocking this for now
                )
                .team(team)
                .points(0)
                .build();
    }

    @Override
    public Long createGame(CreateGameRequest createGameRequest, String principalName) {
        GamePlayer gamePlayer = createGamePlayer(principalName, Team.RED);

        Game game = Game.builder()
                .createdAt(LocalDateTime.now())
                .playersList(new LinkedList<>())
                .rounds(new LinkedList<>())
                .gameType(createGameRequest.getGameType())
                .owner(gamePlayer)
                .joinGameCode(createGameRequest.getJoinGameCode())
                .build();

        Long id = activeGameRepository.put(game);

        game.setName(String.valueOf(id));

        return id;
    }

    @Override
    public Boolean joinGame(Long gameId, JoinGameRequest joinGameRequest, String principalName) {
        Game game = activeGameRepository.findById(gameId)
                .orElseThrow();

        synchronized (game){
            if(game.teamIsFull(joinGameRequest.team) || game.hasStarted() || !game.checkCode(joinGameRequest.joinGameCode)
            || game.playerAlreadyJoined(principalName)){
                return false;
            }
            GamePlayer gamePlayer = createGamePlayer(principalName, joinGameRequest.team);
            game.getPlayersList().add(gamePlayer);

            eventPublisher.publishToLobby(gameId, new PlayersOrderState(
                    game.getPlayersList().stream().map(player -> player.getUser().getUsername()).toList()
            ));
        }

        return true;
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

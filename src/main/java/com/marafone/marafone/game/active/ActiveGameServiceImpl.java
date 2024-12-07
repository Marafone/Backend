package com.marafone.marafone.game.active;

import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.event.outgoing.*;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActiveGameServiceImpl implements ActiveGameService{

    private final ActiveGameRepository activeGameRepository;
    private final EventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final List<Card> allCards;

    private GamePlayer createGamePlayer(String principalName, Team team){
        return GamePlayer.builder()
                .user(userRepository.findByUsername(principalName).get())
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
        Optional<Game> gameOptional = activeGameRepository.findById(gameId);

        if(gameOptional.isEmpty())
            return false;

        Game game = gameOptional.get();

        synchronized (game){
            if(game.teamIsFull(joinGameRequest.team) || game.hasStarted() || !game.checkCode(joinGameRequest.joinGameCode)
            || game.playerAlreadyJoined(principalName)){
                return false;
            }
            GamePlayer gamePlayer = createGamePlayer(principalName, joinGameRequest.team);
            game.getPlayersList().add(gamePlayer);

            eventPublisher.publishToLobby(gameId, new TeamState(game));
        }

        return true;
    }
    /*
        Check if last action was made more than 16 sec ago and if yes then should select random card (similiar logic to selectCard)
        but with random arguments and correct principalName. Should also check if trump suit is null and if game started more than 16 sec
         ago.
     */
    @Override
    public void checkTimeout(Long gameId) {

    }
    @Override
    public void startGame(Long gameId, String principalName) {
        Game game = activeGameRepository.findById(gameId)
                .orElseThrow();

        synchronized (game){
            if(!game.getOwner().getUser().getUsername().equals(principalName)
                || game.getStartedAt() != null || game.getPlayersList().size() != 4)
                return;

            game.setStartedAt(LocalDateTime.now());

            List<Card> cardsInRandomOrder = new ArrayList<>(allCards);
            Collections.shuffle(cardsInRandomOrder);

            int i = 0;
            for(var gamePlayer: game.getPlayersList()){

                gamePlayer.setOwnedCards(new ArrayList<>());

                for(int j = 0; j < 10; j++){
                    gamePlayer.getOwnedCards().add(cardsInRandomOrder.get(i * 10 + j));
                }
                i++;
            }

            game.getRounds().add(Round.builder().actions(new ArrayList<>()).build());

            List<OutEvent> outEvents = new LinkedList<>();
            outEvents.add(new NewRound());
            outEvents.add(new PlayersOrderState(game));
            outEvents.add(new PointState(game));
            outEvents.add(new TeamState(game));
            outEvents.add(new TurnState(game));

            eventPublisher.publishToLobby(gameId, outEvents);

            for(var gamePlayer : game.getPlayersList()){
                eventPublisher.publishToPlayerInTheLobby(gameId, gamePlayer.getUser().getUsername(), new MyCardsState(gamePlayer));
            }
        }

    }
    /*
        Should get game from repo, try to select a card (selecting a card is a write operation so needs to be in synchronized block),
        then after that if the card was selected (player had the card etc.) we do a series of checks and emit events accordingly.
        For example we need to check if the trick ended or if the round ended or if the game ended and emit proper events.
    */
    @Override
    public void selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName) {
        //if trump suit null then dont immeditly return
        //if currentPlayer.next empty then turn ended
        //if turn ended then check if actions.size == 40
        //if round ended then check if points are more than 21 etc.
    }
    @Override
    public void selectSuit(Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName) {
        Game game = activeGameRepository.findById(gameId)
                .orElseThrow();

        synchronized (game){
            Round currentRound = game.getRounds().getLast();

            List<Action> currentActions = currentRound.getActions();
            if(!game.hasStarted() || currentActions.size() != 0){
                return;
            }

            for(var gamePlayer: game.getPlayersList()){
                if(gamePlayer.getUser().getUsername().equals(principalName) && gamePlayer.hasFourOfCoins()){
                    currentRound.setTrumpSuit(trumpSuitSelectEvent.trumpSuit);

                    LinkedList<GamePlayer> enemyTeam =
                            game.getPlayersList().stream().filter(player -> player.getTeam() != gamePlayer.getTeam())
                                    .collect(Collectors.toCollection(LinkedList::new));

                    LinkedList<GamePlayer> newOrderOfPlayers = new LinkedList<>();
                    newOrderOfPlayers.add(gamePlayer);
                    newOrderOfPlayers.add(Math.random() < 0.5 ? enemyTeam.removeFirst() : enemyTeam.removeLast());
                    newOrderOfPlayers.add(game.getPlayersList().stream().filter(
                            player -> player.getTeam() == gamePlayer.getTeam() && !player.equals(gamePlayer)
                    ).findFirst().orElseThrow());
                    newOrderOfPlayers.add(enemyTeam.removeFirst());

                    game.setPlayersList(newOrderOfPlayers);
                    game.setCurrentPlayer(newOrderOfPlayers.listIterator());

                    List<OutEvent> outEvents = new LinkedList<>();
                    outEvents.add(new TurnState(game));
                    outEvents.add(new PlayersOrderState(game));
                    outEvents.add(new TrumpSuitState(game));

                    eventPublisher.publishToLobby(gameId, outEvents);

                    break;
                }
            }
        }
    }
}

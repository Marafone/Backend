package com.marafone.marafone.game.active;

import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.event.outgoing.*;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.user.User;
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

    @Override
    public Long createGame(CreateGameRequest createGameRequest, String principalName) {
        User owner = userRepository.findByUsername(principalName).get();

        GamePlayer gamePlayer = createGamePlayer(owner, Team.RED);

        Game game = Game.builder()
                .createdAt(LocalDateTime.now())
                .playersList(new ArrayList<>())
                .rounds(new LinkedList<>())
                .gameType(createGameRequest.getGameType())
                .owner(owner)
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
            if(!game.getOwner().getUsername().equals(principalName)
                || game.getStartedAt() != null || game.getPlayersList().size() != 4)
                return;

            game.setStartedAt(LocalDateTime.now());

            setupNewRoundAndShuffleCards(game);

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

    @Override
    public void selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName) {
        Game game = activeGameRepository.findById(gameId)
                .orElseThrow();

        synchronized (game){
            if(game.getCurrentPlayer() == null || !game.getCurrentPlayer().hasNext())
                return;

            Round currentRound = game.getRounds().getLast();
            GamePlayer currentPlayer = game.getCurrentPlayer().next();
            Card selectedCard = allCards.get(cardSelectEvent.cardId - 1);

            if(!currentPlayer.getUser().getUsername().equals(principalName) || !currentPlayer.hasCard(selectedCard)
            || (selectedCard.getSuit() != currentRound.getTrumpSuit() && currentPlayer.hasCardOfSuit(currentRound.getTrumpSuit())) ){
                game.getCurrentPlayer().previous();
                return;
            }

            currentRound.getActions().addLast(
                Action.builder().player(currentPlayer).round(currentRound).card(selectedCard).timestamp(LocalDateTime.now()).build()
            );

            currentPlayer.removeCard(selectedCard);

            eventPublisher.publishToPlayerInTheLobby(gameId, principalName, new MyCardsState(currentPlayer));
            eventPublisher.publishToLobby(gameId, new TurnState(game));

            if(!game.turnHasEnded()){
                return;
            }

            List<Action> currentTurn = new LinkedList<>();
            Iterator<Action> actionDescIterator = currentRound.getActions().reversed().iterator();
            for(int i = 0; i < game.getPlayersList().size(); i++){
                currentTurn.add(actionDescIterator.next());
            }

            Action winningAction = getWinningAction(currentTurn);

            int earnedPoints = currentTurn.stream().mapToInt(action -> action.getCard().getRank().getPoints()).sum();
            winningAction.getPlayer().addPoints(earnedPoints);

            List<OutEvent> outEvents = new LinkedList<>();
            outEvents.add(new PointState(game));

            if(!game.roundHasEnded()){
                List<GamePlayer> newOrder = new ArrayList<>();
                for(var gamePlayer: game.getPlayersList()){
                    if(gamePlayer.equals(winningAction.getPlayer()) || !newOrder.isEmpty()){
                        newOrder.addLast(gamePlayer);
                    }
                }
                for(var gamePlayer: game.getPlayersList()){
                    if(newOrder.size() == game.getPlayersList().size())
                        break;

                    newOrder.addLast(gamePlayer);
                }
                game.setPlayersList(newOrder);
                game.setCurrentPlayer(newOrder.listIterator());
                outEvents.add(new PlayersOrderState(game));
            }else{
                winningAction.getPlayer().addBonusPoint();

                if(game.setWinnersIfPossible()){
                    outEvents.add(new WinnerState(game));
                }else{
                    int startingPlayerIndex = game.getRounds().size() % game.getPlayersList().size();

                    List<GamePlayer> newOrder = new ArrayList<>();
                    for(int i = startingPlayerIndex; i < game.getInitialPlayersList().size(); i++){
                        newOrder.addLast(game.getInitialPlayersList().get(i));
                    }
                    for(int i = 0; i < startingPlayerIndex; i++){
                        newOrder.addLast(game.getInitialPlayersList().get(i));
                    }
                    game.setPlayersList(newOrder);
                    game.setCurrentPlayer(newOrder.listIterator());

                    setupNewRoundAndShuffleCards(game);

                    outEvents.add(new PlayersOrderState(game));
                }
            }

            eventPublisher.publishToLobby(gameId, outEvents);
        }

    }

    @Override
    public void selectSuit(Long gameId, TrumpSuitSelectEvent trumpSuitSelectEvent, String principalName) {
        Game game = activeGameRepository.findById(gameId)
                .orElseThrow();

        synchronized (game){
            Round currentRound = game.getRounds().getLast();

            List<Action> currentActions = currentRound.getActions();
            if(!game.hasStarted() || !currentActions.isEmpty()){
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

                    if(game.getRounds().size() == 1)
                        game.setInitialPlayersList(game.getPlayersList());

                    List<OutEvent> outEvents = new LinkedList<>();
                    outEvents.add(new PlayersOrderState(game));
                    outEvents.add(new TrumpSuitState(game));

                    eventPublisher.publishToLobby(gameId, outEvents);

                    break;
                }
            }
        }
    }

    @Override
    public void reconnectToGame(Long gameId, String principalName) {
        Game game = activeGameRepository.findById(gameId)
                .orElseThrow();

        synchronized (game){
            for(var gamePlayer: game.getPlayersList()){
                if(gamePlayer.getUser().getUsername().equals(principalName)){


                    List<OutEvent> outEvents = new LinkedList<>();
                    if(game.hasStarted()){
                        OutEvent cardsState = new MyCardsState(gamePlayer);
                        eventPublisher.publishToPlayerInTheLobby(gameId, principalName, cardsState);

                        outEvents.add(new PlayersOrderState(game));
                        outEvents.add(new PointState(game));
                        outEvents.add(new TeamState(game));
                        outEvents.add(new TrumpSuitState(game));
                        outEvents.add(new TurnState(game));

                        if(game.getWinnerTeam() != null)
                            outEvents.add(new WinnerState(game));
                    }else
                        outEvents.add(new TeamState(game));

                    eventPublisher.publishToLobby(gameId, outEvents);

                    break;
                }
            }
        }
    }

    private GamePlayer createGamePlayer(User user, Team team){
        return GamePlayer.builder()
                .user(user)
                .team(team)
                .points(0)
                .build();
    }

    private GamePlayer createGamePlayer(String principalName, Team team){
        return createGamePlayer(userRepository.findByUsername(principalName).get(), team);
    }

    private void setupNewRoundAndShuffleCards(Game game){
        List<Card> cardsInRandomOrder = new ArrayList<>(allCards);
        Collections.shuffle(cardsInRandomOrder);

        int i = 0;
        for(var gamePlayer: game.getPlayersList()){

            gamePlayer.setOwnedCards(new LinkedList<>());

            for(int j = 0; j < 10; j++){
                gamePlayer.getOwnedCards().add(cardsInRandomOrder.get(i * 10 + j));
            }
            i++;
        }

        game.getRounds().add(Round.builder().actions(new LinkedList<>()).build());
    }

    public Action getWinningAction(List<Action> currentTurn){
        return currentTurn.stream().max((a, b) -> {
            Suit trumpSuit = a.getRound().getTrumpSuit();
            Suit aSuit = a.getCard().getSuit();
            Suit bSuit = b.getCard().getSuit();
            if (aSuit != bSuit && (aSuit == trumpSuit || bSuit == trumpSuit)) {
                if (aSuit == trumpSuit) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                CardRank aRank = a.getCard().getRank();
                CardRank bRank = b.getCard().getRank();
                return aRank.compareTo(bRank);
            }
        }).orElseThrow();
    }
}

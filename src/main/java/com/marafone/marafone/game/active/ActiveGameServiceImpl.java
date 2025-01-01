package com.marafone.marafone.game.active;

import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.ended.EndedGameService;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.event.outgoing.*;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.mappers.GameMapper;
import com.marafone.marafone.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.marafone.marafone.errors.SelectCardErrorMessages.*;
import static com.marafone.marafone.game.model.JoinGameResult.*;

@Service
@RequiredArgsConstructor
public class ActiveGameServiceImpl implements ActiveGameService{

    private final ActiveGameRepository activeGameRepository;
    private final EndedGameService endedGameService;
    private final EventPublisher eventPublisher;
    private final List<Card> allCards;
    private final GameMapper gameMapper;
    private final Random r = new Random();

    @Override
    public List<GameDTO> getWaitingGames() {
        List<Game> allPublicGames = activeGameRepository.getWaitingGames();
        return allPublicGames.stream()
                        .map(gameMapper::toGameDTO)
                        .toList();
    }

    @Override
    public Long createGame(CreateGameRequest createGameRequest, User user) {
        GamePlayer gamePlayer = createGamePlayer(user, Team.RED);

        Game game = Game.builder()
                .createdAt(LocalDateTime.now())
                .playersList(new ArrayList<>(List.of(gamePlayer)))
                .rounds(new LinkedList<>())
                .name(createGameRequest.getGameName())
                .gameType(createGameRequest.getGameType())
                .owner(user)
                .joinGameCode(createGameRequest.getJoinGameCode())
                .build();

        return activeGameRepository.put(game);
    }

    // checks if game with arg name already exists
    // among games waiting for players to join

    public boolean doesNotStartedGameAlreadyExist(String name) {
        return activeGameRepository.getWaitingGames()
                        .stream()
                        .map(Game::getName)
                        .anyMatch(gameName -> gameName.equals(name));
    }

    @Override
    public JoinGameResult joinGame(Long gameId, JoinGameRequest joinGameRequest, User user) {
        Optional<Game> gameOptional = activeGameRepository.findById(gameId);

        if(gameOptional.isEmpty())
            return JoinGameResult.GAME_NOT_FOUND;

        Game game = gameOptional.get();

        synchronized (game){

            if (!game.anyTeamNotFull())
                return TEAMS_FULL;
            else if (game.hasStarted())
                return GAME_ALREADY_STARTED;
            else if (!game.checkCode(joinGameRequest.joinGameCode))
                return INCORRECT_PASSWORD;
            else if (game.playerAlreadyJoined(user.getUsername()))
                return PLAYER_ALREADY_JOINED;

            GamePlayer gamePlayer;
            if (!game.teamIsFull(Team.RED))
                gamePlayer = createGamePlayer(user, Team.RED);
            else // we are sure that second team is not full because of validation
                gamePlayer = createGamePlayer(user, Team.BLUE);

            game.getPlayersList().add(gamePlayer);

            eventPublisher.publishToLobby(gameId, new PlayerJoinedEvent(user.getUsername(), gamePlayer.getTeam()));
        }

        return JoinGameResult.SUCCESS;
    }
    // TODO when owner leaves - make other player the owner
    // TODO when last person leaves - remove the game
    public void leaveGame(Long gameId, User user) {
        Optional<Game> gameOptional = activeGameRepository.findById(gameId);
        if (gameOptional.isEmpty()) return;

        Game game = gameOptional.get();
        Optional<GamePlayer> optionalGamePlayer;
        synchronized (game) {
             optionalGamePlayer = game
                    .getPlayersList()
                    .stream()
                    .filter(player -> player.getUser().getUsername().equals(user.getUsername()))
                    .findFirst();
            if (optionalGamePlayer.isEmpty()) return;

            GamePlayer gamePlayerToRemove = optionalGamePlayer.get();
            game.getPlayersList().remove(gamePlayerToRemove);
        }

        eventPublisher.publishToLobby(gameId, new PlayerLeftEvent(user.getUsername()));

    }

    public void changeTeam(Long gameId, Team team, User user) {
        Optional<Game> gameOptional = activeGameRepository.findById(gameId);
        if (gameOptional.isEmpty())
            return;

        Game game = gameOptional.get();

        synchronized (game) {

            if (game.teamIsFull(team))
                return;

            GamePlayer gamePlayer = game.findGamePlayerByUsername(user.getUsername());
            if (gamePlayer == null || gamePlayer.getTeam() == team)
                return;

            gamePlayer.setTeam(team);

        }

        eventPublisher.publishToLobby(gameId, new TeamState(game));

    }

    public Map<Team, List<GamePlayer>> getGameTeams(Long gameId) {
        Game game = findGameById(gameId).orElse(null);
        if (game == null)
            return null; // this game does not exist

        synchronized (game){
            Map<Team, List<GamePlayer>> output = new EnumMap<>(Team.class);
            output.put(Team.RED, new ArrayList<>());
            output.put(Team.BLUE, new ArrayList<>());
            for (var player: game.getPlayersList()) {
                output.get(player.getTeam()).add(player);
            }
            return output;
        }
    }

    private Optional<Game> findGameById(Long gameId) {
        return activeGameRepository.findById(gameId);
    }

    @Override
    public void checkTimeout(Long gameId) {

    }

    @Override
    public void startGame(Long gameId, String principalName) {
        Optional<Game> gameOptional = activeGameRepository.findById(gameId);
        if (gameOptional.isEmpty())
            return;

        Game game = gameOptional.get();

        synchronized (game){
            // will be replaced with proper events sending in future
            if(!game.getOwner().getUsername().equals(principalName))
                return;
            else if (game.getStartedAt() != null)
                return;
            else if (game.anyTeamNotFull())
                return;

            game.setStartedAt(LocalDateTime.now());

            ShuffleCards(game);

            setInitialOrder(game);

            game.addRound();

            List<OutEvent> outEvents = new LinkedList<>();
            outEvents.add(new GameStartedEvent());

            eventPublisher.publishToLobby(gameId, outEvents);
        }
    }

    @Override
    public void selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName) {
        Game game = activeGameRepository.findById(gameId)
                .orElseThrow();

        synchronized (game){
            if(game.getCurrentPlayer() == null || !game.getCurrentPlayer().hasNext()) {
                return;
            }

            Round currentRound = game.getRounds().getLast();
            GamePlayer currentPlayer = game.getCurrentPlayer().next();
            Card selectedCard = allCards.get(cardSelectEvent.cardId - 1);
            ErrorEvent errorEvent = null;

            if (!currentPlayer.getUser().getUsername().equals(principalName))
                errorEvent = new ErrorEvent(NOT_YOUR_TURN.formatMessage(currentPlayer.getUser().getUsername()));
            else if (!currentPlayer.hasCard(selectedCard))
                errorEvent = new ErrorEvent(CARD_NOT_IN_HAND.getMessage());
            else if (currentRound.getTrumpSuit() == null)
                errorEvent = new ErrorEvent(TRUMP_SUIT_NOT_SELECTED.getMessage());
            else if (selectedCard.getSuit() != currentRound.getTrumpSuit()
                    && currentPlayer.hasCardOfSuit(currentRound.getTrumpSuit()))
                errorEvent = new ErrorEvent(INVALID_TRUMP_SUIT_PLAY.formatMessage(currentRound.getTrumpSuit()));

            if (errorEvent != null) {
                game.getCurrentPlayer().previous();
                eventPublisher.publishToPlayerInTheLobby(gameId, principalName, errorEvent);
                return;
            }

            currentRound.getActions().addLast(
                Action.builder().player(currentPlayer).round(currentRound).card(selectedCard).timestamp(LocalDateTime.now()).build()
            );

            currentPlayer.removeCard(selectedCard);

            eventPublisher.publishToPlayerInTheLobby(gameId, principalName, new MyCardsState(currentPlayer));
            eventPublisher.publishToLobby(gameId, new TurnState(game));

            if(!game.turnHasEnded()) return;

            List<Action> currentTurn = currentRound.getLastNActions(4);

            Action winningAction = getWinningAction(currentTurn);

            int earnedPoints = currentTurn.stream().mapToInt(action -> action.getCard().getRank().getPoints()).sum();
            winningAction.getPlayer().addPoints(earnedPoints);

            List<OutEvent> outEvents = new LinkedList<>();
            outEvents.add(new PointState(game));

            if(game.roundHasEnded()){
                winningAction.getPlayer().addBonusPoint();

                if(game.setWinnersIfPossible()){
                    outEvents.add(new WinnerState(game));
                    endedGameService.saveEndedGame(game);
                    eventPublisher.publishToLobby(gameId, outEvents);
                    return;
                }else{
                    game.setNewOrderAfterRoundEnd();

                    ShuffleCards(game);
                    game.addRound();

                    outEvents.add(new NewRound());
                    outEvents.add(new MyCardsState(currentPlayer));
                }
            }else{
                game.setNewOrderAfterTurnEnd(winningAction.getPlayer());
            }
            outEvents.add(new PlayersOrderState(game));
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

            GamePlayer gamePlayer = game.findGamePlayerByUsername(principalName);
            GamePlayer playerToMove = game.getCurrentPlayer().next();
            game.getCurrentPlayer().previous();
            if(
                gamePlayer == null
                || (game.getRounds().size() == 1 && !gamePlayer.hasFourOfCoins()) //only in first round
                || (game.getRounds().size() != 1 && !gamePlayer.equals(playerToMove))
            )
                return;

            currentRound.setTrumpSuit(trumpSuitSelectEvent.trumpSuit);

            List<OutEvent> outEvents = new LinkedList<>();
            outEvents.add(new TrumpSuitState(game));

            eventPublisher.publishToLobby(gameId, outEvents);
        }
    }

    @Override
    public void selectRandomCard(Long gameId, String principalName) {

        Optional<Game> gameOptional = activeGameRepository.findById(gameId);
        if (gameOptional.isEmpty())
            return;

        Game game = gameOptional.get();
        Suit currentTrumpSuit = game.getRounds().getLast().getTrumpSuit();
        List<Card> playerCards = getGamePlayerCards(gameId, principalName);

        int selectedCardIndex = r.nextInt(playerCards.size());
        Card cardToRemove = playerCards.get(selectedCardIndex);

        // check if card with selected trump suit exist

        Optional<Card> result = playerCards.stream()
                .filter(card -> card.getSuit() == currentTrumpSuit)
                .findAny();

        if (result.isPresent()) // roll until we get card with current round trump suit
            while (cardToRemove.getSuit() != currentTrumpSuit) {
                selectedCardIndex = r.nextInt(playerCards.size());
                cardToRemove = playerCards.get(selectedCardIndex);
            }

        selectCard(gameId, new CardSelectEvent(Math.toIntExact(cardToRemove.getId())), principalName);
    }

    @Override
    public void reconnectToGame(Long gameId, String principalName) {
        Game game = activeGameRepository.findById(gameId)
                .orElseThrow();

        synchronized (game){
            GamePlayer gamePlayer = game.findGamePlayerByUsername(principalName);

            if(gamePlayer == null)
                return;

            List<OutEvent> outEvents = new LinkedList<>();
            if(game.hasStarted()){
                OutEvent cardsState = new MyCardsState(gamePlayer);
                eventPublisher.publishToPlayerInTheLobby(gameId, principalName, cardsState);

                outEvents.add(new PlayersOrderState(game));
                outEvents.add(new TeamState(game));
                outEvents.add(new PointState(game));
                outEvents.add(new TrumpSuitState(game));
                outEvents.add(new TurnState(game));

                if(game.getWinnerTeam() != null)
                    outEvents.add(new WinnerState(game));
            }else
                outEvents.add(new TeamState(game));

            eventPublisher.publishToLobby(gameId, outEvents);
        }
    }

    private GamePlayer createGamePlayer(User user, Team team){
        return GamePlayer.builder()
                .user(user)
                .team(team)
                .points(0)
                .build();
    }

    private void ShuffleCards(Game game){
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

    private void setInitialOrder(Game game){
        GamePlayer gamePlayer = game.getPlayersList().stream().filter(GamePlayer::hasFourOfCoins).findFirst().orElseThrow();

        LinkedList<GamePlayer> enemyTeam = game.getPlayersList().stream().filter(player -> player.getTeam() != gamePlayer.getTeam())
                .collect(Collectors.toCollection(LinkedList::new));

        LinkedList<GamePlayer> startingOrderOfPlayers = new LinkedList<>();
        startingOrderOfPlayers.add(gamePlayer);
        startingOrderOfPlayers.add(Math.random() < 0.5 ? enemyTeam.removeFirst() : enemyTeam.removeLast());
        startingOrderOfPlayers.add(game.getPlayersList().stream().filter(
                player -> player.getTeam() == gamePlayer.getTeam() && !player.equals(gamePlayer)
        ).findFirst().orElseThrow());
        startingOrderOfPlayers.add(enemyTeam.removeFirst());

        game.setPlayersList(startingOrderOfPlayers);
        game.setCurrentPlayer(startingOrderOfPlayers.listIterator());
        game.setInitialPlayersList(new ArrayList<>(game.getPlayersList()));
    }

    private List<Card> getGamePlayerCards(Long gameId, String principalName) {
        Optional<Game> gameOptional = findGameById(gameId);
        if (gameOptional.isEmpty())
            return null;

        GamePlayer gamePlayer = gameOptional.get()
                .getPlayersList()
                .stream()
                .filter(player -> player.getUser().getUsername().equals(principalName))
                .findFirst()
                .orElse(null);
        if (gamePlayer == null)
            return null;

        return gamePlayer.getOwnedCards();
    }
}

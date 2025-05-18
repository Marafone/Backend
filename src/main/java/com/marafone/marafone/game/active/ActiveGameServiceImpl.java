package com.marafone.marafone.game.active;

import com.marafone.ai.DummyData;
import com.marafone.ai.MarafoneAI;
import com.marafone.ai.Move;
import com.marafone.ai.MoveApplier;
import com.marafone.marafone.AiManager;
import com.marafone.ai.MarafoneAI;
import com.marafone.ai.Move;
import com.marafone.ai.MoveApplier;
import com.marafone.marafone.AiManager;
import com.marafone.marafone.errors.ChangeTeamErrorMessages;
import com.marafone.marafone.errors.StartGameErrorMessages;
import com.marafone.marafone.exception.GameNotFoundException;
import com.marafone.marafone.exception.SelectCardException;
import com.marafone.marafone.game.broadcaster.EventPublisher;
import com.marafone.marafone.game.context.SelectCardContext;
import com.marafone.marafone.game.dto.GameDTO;
import com.marafone.marafone.game.ended.EndedGameService;
import com.marafone.marafone.game.event.incoming.CardSelectEvent;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.event.incoming.JoinGameRequest;
import com.marafone.marafone.game.event.incoming.TrumpSuitSelectEvent;
import com.marafone.marafone.game.event.outgoing.*;
import com.marafone.marafone.game.random.RandomAssigner;
import com.marafone.marafone.game.model.*;
import com.marafone.marafone.game.response.GameActionResponse;
import com.marafone.marafone.game.response.JoinGameResult;
import com.marafone.marafone.mappers.GameMapper;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static com.marafone.ai.TrainingLoop.getValidMoves;
import static com.marafone.marafone.errors.SelectCardErrorMessages.*;
import static com.marafone.marafone.game.response.JoinGameResult.*;

@Service
@RequiredArgsConstructor
public class ActiveGameServiceImpl implements ActiveGameService{

    private final ActiveGameRepository activeGameRepository;
    private final EndedGameService endedGameService;
    private final UserService userService;
    private final EventPublisher eventPublisher;
    private final List<Card> allCards;
    private final GameMapper gameMapper;

    // Map to store AI instances
    private final Map<String, MarafoneAI> aiPlayers = new HashMap<>();

    private final RandomAssigner randomAssigner;

    public GameDTO getWaitingGameById(Long id) {
        Optional<Game> gameOptional = activeGameRepository.findById(id);
        if (gameOptional.isEmpty())
            throw new GameNotFoundException("Game does not exist. Check game code and try again.");
        Game game = gameOptional.get();
        return gameMapper.toGameDTO(game);
    }

    @Override
    public List<GameDTO> getWaitingGames() {
        List<Game> allPublicGames = activeGameRepository.getWaitingGames();
        return allPublicGames.stream()
                        .map(gameMapper::toGameDTO)
                        .toList();
    }

    public Optional<Long> getActiveGameForPlayer(String playerName) {
        List<Game> startedGames = activeGameRepository.getStartedGames();
        return startedGames.stream()
                           .filter(game -> game.getPlayersList()
                                     .stream()
                                     .map(gp -> gp.getUser().getUsername())
                                     .anyMatch(username -> username.equals(playerName)))
                           .map(Game::getId)
                           .findFirst();
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
                .pointsToWinGame(createGameRequest.getPointsToWin() * 3)
                .build();

        return activeGameRepository.put(game);
    }

    @Override
    public boolean doesNotStartedGameAlreadyExist(String name) {
        return activeGameRepository.getWaitingGames()
                        .stream()
                        .map(Game::getName)
                        .anyMatch(gameName -> gameName.equals(name));
    }

    @Override
    public boolean checkIfUserIsInGame(User user) {
        return getActiveGameForPlayer(user.getUsername()).isPresent();
    }

    @Override
    public JoinGameResult joinGame(Long gameId, JoinGameRequest joinGameRequest, User user) {
        Optional<Game> gameOptional = activeGameRepository.findById(gameId);

        if (gameOptional.isEmpty())
            return JoinGameResult.GAME_NOT_FOUND;
        else if (user.isInGame(this::checkIfUserIsInGame))
            return JoinGameResult.PLAYER_DURING_ANOTHER_GAME;

        Game game = gameOptional.get();

        synchronized (game){

            if (!game.anyTeamNotFull())
                return TEAMS_FULL;
            else if (game.hasStarted())
                return GAME_ALREADY_STARTED;
            else if (!game.checkCode(joinGameRequest.joinGameCode()))
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

    @Override
    public void leaveGame(Long gameId, User user) {
        Optional<Game> gameOptional = activeGameRepository.findById(gameId);
        if (gameOptional.isEmpty()) return;

        Game game = gameOptional.get();

        synchronized (game) {
            if (game.hasStarted())
                return;

            Optional<GamePlayer> optionalGamePlayer = game
                    .getPlayersList()
                    .stream()
                    .filter(player -> player.getUser().getUsername().equals(user.getUsername()))
                    .findFirst();
            if (optionalGamePlayer.isEmpty()) return;

            GamePlayer gamePlayerToRemove = optionalGamePlayer.get();
            game.getPlayersList().remove(gamePlayerToRemove);

            // if last person left - remove the game
            if (game.getPlayersList().isEmpty()) {
                activeGameRepository.removeById(gameId);
                return;
            }

            eventPublisher.publishToLobby(gameId, new PlayerLeftEvent(user.getUsername()));

            // when owner leaves, first player of players list becomes new owner
            if (user.getUsername().equals(game.getOwner().getUsername())) {
                User newOwner = game.getPlayersList().getFirst().getUser();
                game.setOwner(newOwner);
                eventPublisher.publishToLobby(gameId, new OwnerEvent(newOwner.getUsername(), true));
            }
        }
    }

    @Override
    public void changeTeam(Long gameId, Team team, User user) {
        Optional<Game> gameOptional = activeGameRepository.findById(gameId);
        if (gameOptional.isEmpty())
            return;

        Game game = gameOptional.get();

        synchronized (game) {

            GamePlayer gamePlayer = game.findGamePlayerByUsername(user.getUsername());

            if (gamePlayer == null) {
                return;
            } else if (gamePlayer.getTeam() == team) {
                eventPublisher.publishToPlayerInTheLobby(
                        gameId,
                        user.getUsername(),
                        new ErrorEvent(ChangeTeamErrorMessages.SAME_TEAM.getMessage())
                );
                return;
            } else if (game.teamIsFull(team)) {
                eventPublisher.publishToPlayerInTheLobby(
                        gameId,
                        user.getUsername(),
                        new ErrorEvent(ChangeTeamErrorMessages.TEAM_IS_FULL.getMessage())
                );
                return;
            }

            gamePlayer.setTeam(team);

            eventPublisher.publishToLobby(gameId, new TeamState(game));

        }
    }

    @Override
    public AddAIResult addAI(Long gameId, Team team, User user) {
        try {
            // Load the trained AI
            MarafoneAI trainedAI = MarafoneAI.load("trained_ai.ser");

            // Get an available AI user
            User aiUser = AiManager.getAvailableAI();
            if (aiUser == null) {
                return AddAIResult.MAX_AI_REACHED;
            }

            // Join the AI to the game
            JoinGameRequest joinRequest = new JoinGameRequest(activeGameRepository.findById(gameId).get().getJoinGameCode()); // Use the correct game code
            JoinGameResult result = this.joinGame(gameId, joinRequest, aiUser);

            if (result == SUCCESS) {
                // Store the AI instance
                aiPlayers.put(aiUser.getUsername(), trainedAI);
                return AddAIResult.SUCCESS;
            } else {
                AiManager.releaseAI(aiUser); // Release AI if joining fails
                return AddAIResult.FAILED_TO_ADD;
            }
        } catch (IOException | ClassNotFoundException e) {
            return AddAIResult.AI_LOAD_ERROR;
        }
    }

    @Override
    public MakeAIMoveResult makeAIMove(Long gameId, String playerUsername) {
        Game game = activeGameRepository.findById(gameId).orElseThrow();

        synchronized (game) {
            // Retrieve the AI instance for this player
            MarafoneAI trainedAI = aiPlayers.get(playerUsername);
            if (trainedAI == null) {
                return MakeAIMoveResult.NO_AI_FOUND;
            }

            // Get the AI player
            GamePlayer aiPlayer = game.getPlayersList().stream()
                    .filter(p -> p.getUser().getUsername().equals(playerUsername))
                    .findFirst()
                    .orElseThrow();

            // Get valid moves and let the AI choose one
            List<Move> validMoves = getValidMoves(game, aiPlayer);
            if (validMoves.isEmpty()) {
                return MakeAIMoveResult.NO_VALID_MOVES;
            }

            Move chosenMove = trainedAI.selectMove(validMoves);
            if (chosenMove.getCard() == null && chosenMove.getSuit() != null) {
                // the AI is the first to play needs to play two moves
                MoveApplier.applyMove(game, aiPlayer, chosenMove, this);
                List<Move> validCards = getValidMoves(game, aiPlayer);
                Move chosenCard = trainedAI.selectMove(validCards);
                MoveApplier.applyMove(game, aiPlayer, chosenCard, this);
            } else {
                MoveApplier.applyMove(game, aiPlayer, chosenMove, this);
            }

            return MakeAIMoveResult.SUCCESS;
        }
    }

    private Optional<Game> findGameById(Long gameId) {
        return activeGameRepository.findById(gameId);
    }

    @Override
    public void checkTimeout(Long gameId) {
        Game game = findGameById(gameId).orElseThrow();

        synchronized (game){
            if(!game.hasStarted() || game.hasEnded())
                return;

            Round lastRound = game.getRounds().getLast();

            if(lastRound.isTrumpSuitSelected()){//check cards
                LocalDateTime lastAction = !lastRound.getActions().isEmpty() ? lastRound.getActions().getLast().getTimestamp()
                    : game.getStartedAt();

                if(!LocalDateTime.now().isAfter(lastAction.plusSeconds(15)))
                    return;

                GamePlayer currentPlayer = game.getCurrentPlayerWithoutIterating();
                Card randomCard = game.getLeadingSuit() == null ?
                        randomAssigner.getRandomCorrectCard(currentPlayer.getOwnedCards()) :
                        randomAssigner.getRandomCorrectCard(currentPlayer.getOwnedCards(), game.getLeadingSuit());

                selectCard(gameId, new CardSelectEvent(randomCard.getId()), currentPlayer.getUser().getUsername());
            }else{//check trump suit

                if(!LocalDateTime.now().isAfter(game.getStartedAt().plusSeconds(15)))
                    return;

                Suit randomSuit = randomAssigner.getRandomTrumpSuit();
                String username = game.getRounds().size() != 1 ? game.getCurrentPlayerWithoutIterating().getUser().getUsername()
                    : game.getPlayersList().stream().filter(GamePlayer::hasFourOfCoins).findFirst().get().getUser().getUsername();

                selectSuit(gameId, new TrumpSuitSelectEvent(randomSuit), username);
            }
        }
    }

    @Override
    public void startGame(Long gameId, String principalName) {
        Optional<Game> gameOptional = activeGameRepository.findById(gameId);
        if (gameOptional.isEmpty())
            return;

        Game game = gameOptional.get();

        synchronized (game){
            if(!game.getOwner().getUsername().equals(principalName)) {
                eventPublisher.publishToPlayerInTheLobby(
                        gameId,
                        principalName,
                        new ErrorEvent(StartGameErrorMessages.OWNER_MISMATCH.getMessage())
                );
                return;
            } else if (game.getStartedAt() != null) {
                eventPublisher.publishToPlayerInTheLobby(
                    gameId,
                    principalName,
                    new ErrorEvent(StartGameErrorMessages.GAME_ALREADY_STARTED.getMessage())
                );
                return;
            } else if (game.anyTeamNotFull()) {
                eventPublisher.publishToPlayerInTheLobby(
                        gameId,
                        principalName,
                        new ErrorEvent(StartGameErrorMessages.TEAMS_NOT_FULL.getMessage())
                );
                return;
            }

            game.setStartedAt(LocalDateTime.now());
            markUsersAsInGame(game.getPlayersList().stream().map(GamePlayer::getUser).toList());
            randomAssigner.assignRandomCardsToPlayers(game.getPlayersList());

            List<GamePlayer> startingOrderOfPlayers = randomAssigner.assignRandomInitialOrder(game.getPlayersList());
            game.setPlayersList(startingOrderOfPlayers);
            game.setCurrentPlayer(startingOrderOfPlayers.listIterator());
            game.setInitialPlayersList(new ArrayList<>(game.getPlayersList()));

            game.addRound();

            List<OutEvent> outEvents = new LinkedList<>();
            outEvents.add(new GameStartedEvent());
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
    public SelectCardResult selectCard(Long gameId, CardSelectEvent cardSelectEvent, String principalName) {
        Game game = activeGameRepository.findById(gameId)
                .orElseThrow();

        synchronized (game) {
            if (game.getCurrentPlayer() == null || !game.getCurrentPlayer().hasNext()) {
                return SelectCardResult.NOT_YOUR_TURN;
            }

            GamePlayer currentPlayer = game.getCurrentPlayerWithoutIterating();
            Round currentRound = game.getRounds().getLast();
            Card selectedCard = allCards.get((int) (cardSelectEvent.cardId - 1));

            SelectCardContext selectCardContext = new SelectCardContext(gameId, currentPlayer, currentRound, selectedCard);

            validateSelectCard(game.getLeadingSuit(), selectCardContext, principalName);
            updateGameState(game, selectCardContext);
            sendCurrentTurnChangeMessages(game, currentPlayer);

            if(!game.turnHasEnded()) {
                eventPublisher.publishToLobby(
                        gameId,
                        new NextPlayerState(game.getCurrentPlayerWithoutIterating()
                                .getUser()
                                .getUsername(), false)
                );
                return SelectCardResult.SUCCESS;
            }

            List<Action> currentTurn = currentRound.getLastNActions(4);
            GamePlayer winningPlayer = findWinningPlayer(currentTurn);

            handleEndedTurn(game, currentTurn, winningPlayer);

            if (game.roundHasEnded()) {
                handleEndedRound(game, winningPlayer);
                // Release AI players
                game.getPlayersList().stream()
                        .map(GamePlayer::getUser)
                        .filter(user -> user.getUsername().startsWith("AI_"))
                        .forEach(AiManager::releaseAI);

                return SelectCardResult.GAME_ENDED;
            }else
                handleNewTurn(game, winningPlayer);

            if (!game.isEnded())
                eventPublisher.publishToLobby(gameId, List.of(new PointState(game), new PlayersOrderState(game)));
        }

            return SelectCardResult.SUCCESS;
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
            GamePlayer playerToMove = game.getCurrentPlayerWithoutIterating();
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

    public void sendCall(Long gameId, Call call) {
        if (activeGameRepository.findById(gameId).isEmpty())
            return;

        eventPublisher.publishToLobby(gameId, new CallState(call));
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

                outEvents.add(new MyCardsState(gamePlayer));
                outEvents.add(new PlayersOrderState(game));
                outEvents.add(new TeamState(game));
                outEvents.add(new PointState(game));
                outEvents.add(new TrumpSuitState(game));
                outEvents.add(new TurnState(game));

                if(game.getWinnerTeam() != null)
                    outEvents.add(new WinnerState(game));
            }else {
                outEvents.add(new TeamState(game));
                outEvents.add(new OwnerEvent(game.getOwner().getUsername(), false));
            }

            eventPublisher.publishToPlayerInTheLobby(gameId, principalName, outEvents);
        }
    }

    private GamePlayer createGamePlayer(User user, Team team){
        return GamePlayer.builder()
                .user(user)
                .team(team)
                .points(0)
                .build();
    }

    public Action getWinningAction(List<Action> currentTurn){
        return currentTurn.stream().max((a, b) -> {
            Suit trumpSuit = a.getRound().getTrumpSuit();
            Suit leadingSuit = currentTurn.getFirst().getCard().getSuit();
            Suit aSuit = a.getCard().getSuit();
            Suit bSuit = b.getCard().getSuit();
            if (aSuit != bSuit && (aSuit == trumpSuit || bSuit == trumpSuit || aSuit == leadingSuit || bSuit == leadingSuit)) {
                if (aSuit == trumpSuit) {
                    return 1;
                } else if(bSuit == trumpSuit){
                    return -1;
                }else if(aSuit == leadingSuit)
                    return 1;
                else//bSuit == leadingSuit
                    return -1;
            } else {
                CardRank aRank = a.getCard().getRank();
                CardRank bRank = b.getCard().getRank();
                return aRank.compareTo(bRank);
            }
        }).orElseThrow();
    }

    private void validateSelectCard(Suit leadingSuit, SelectCardContext selectCardContext, String principalName) {
        GamePlayer currentPlayer = selectCardContext.currentPlayer();
        Card selectedCard = selectCardContext.selectedCard();
        Round currentRound = selectCardContext.currentRound();

        if (!isRequestPlayerTurn(currentPlayer, principalName))
            throw new SelectCardException(NOT_YOUR_TURN.formatMessage(currentPlayer.getUser().getUsername()));
        else if (!currentPlayer.hasCard(selectedCard))
            throw new SelectCardException(CARD_NOT_IN_HAND.getMessage());
        else if (!currentRound.isTrumpSuitSelected())
            throw new SelectCardException(TRUMP_SUIT_NOT_SELECTED.getMessage());
        else if (isInvalidLeadingSuitPlayed(leadingSuit, currentPlayer, selectedCard))
            throw new SelectCardException(INVALID_LEADING_SUIT_PLAY.formatMessage(leadingSuit));
    }

    private boolean isRequestPlayerTurn(GamePlayer actualPlayer, String requestPlayerName) {
        return actualPlayer.getUser().getUsername().equals(requestPlayerName);
    }

    private boolean isInvalidLeadingSuitPlayed(Suit leadingSuit, GamePlayer gamePlayer, Card selectedCard) {
        return leadingSuit != null
               && selectedCard.getSuit() != leadingSuit
               && gamePlayer.hasCardOfSuit(leadingSuit);
    }

    private void updateGameState(Game game, SelectCardContext selectCardContext) {
        GamePlayer currentPlayer = selectCardContext.currentPlayer();
        Round currentRound = selectCardContext.currentRound();
        Card selectedCard = selectCardContext.selectedCard();

        Action actionToAdd = Action.builder()
                .player(currentPlayer)
                .round(currentRound)
                .card(selectedCard)
                .timestamp(LocalDateTime.now())
                .build();

        game.getCurrentPlayer().next();
        currentRound.addNewAction(actionToAdd);
        game.setLeadingSuitIfUnset(selectedCard.getSuit());
        currentPlayer.removeCard(selectedCard);
    }

    private void sendCurrentTurnChangeMessages(Game game, GamePlayer currentPlayer) {
        eventPublisher.publishToPlayerInTheLobby(
                game.getId(),
                currentPlayer.getUser().getUsername(),
                new MyCardsState(currentPlayer)
        );
        eventPublisher.publishToLobby(game.getId(), new TurnState(game));
    }

    private void handleEndedTurn(Game game, List<Action> currentTurn, GamePlayer winningPlayer) {
        game.setLeadingSuit(null);
        updateWinningPlayerPoints(currentTurn, winningPlayer);
    }

    private void updateWinningPlayerPoints(List<Action> turn, GamePlayer winningPlayer) {
        int earnedPoints = findPointsEarnedInTurn(turn);
        winningPlayer.addPoints(earnedPoints);
    }

    private int findPointsEarnedInTurn(List<Action> turn) {
        return turn.stream()
                .mapToInt(action -> action.getCard().getRank().getPoints())
                .sum();
    }

    private GamePlayer findWinningPlayer(List<Action> turn) {
        Action winningAction = getWinningAction(turn);
        return winningAction.getPlayer();
    }

    private void handleEndedRound(Game game, GamePlayer winningPlayer) {
        winningPlayer.addBonusPoint();

        if (game.isSettingWinnersPossible()) {
            game.setWinners();
            userService.updateUsersStats(
                    game.getGamePlayersFromTeam(Team.RED).stream().map(GamePlayer::getUser).toList(),
                    game.getGamePlayersFromTeam(Team.BLUE).stream().map(GamePlayer::getUser).toList(),
                    game.getWinnerTeam()
            );

            endedGameService.saveEndedGame(game);
            markUsersAsNotInGame(game.getPlayersList().stream().map(GamePlayer::getUser).toList());
            activeGameRepository.removeById(game.getId());
            eventPublisher.publishToLobby(game.getId(), List.of(new PointState(game), new WinnerState(game)));
        } else {
            reduceTeamsPoints(game);
            prepareGameForNextRound(game);
            sendNewRoundMessages(game);
        }
    }

    private void reduceTeamsPoints(Game game) {
        int redTeamPoints = game.getTeamPoints(Team.RED);
        int blueTeamPoints = game.getTeamPoints(Team.BLUE);

        GamePlayer redTeamTopScorer = game.findTopScorerInTeam(Team.RED);
        GamePlayer blueTeamTopScorer = game.findTopScorerInTeam(Team.BLUE);

        redTeamTopScorer.subtractPoints(redTeamPoints % 3);
        blueTeamTopScorer.subtractPoints(blueTeamPoints % 3);
    }

    private void markUsersAsInGame(List<User> users) {
        for (var user: users)
            user.setInGame(true);
    }

    private void markUsersAsNotInGame(List<User> users) {
        for (var user: users)
            user.setInGame(false);
    }

    private void prepareGameForNextRound(Game game) {
        game.setNewOrderAfterRoundEnd();
        randomAssigner.assignRandomCardsToPlayers(game.getPlayersList());
        game.addRound();
    }

    private void sendNewRoundMessages(Game game) {
        Long gameId = game.getId();
        GamePlayer currentPlayer = game.getCurrentPlayerWithoutIterating();
        eventPublisher.publishToLobby(gameId, new NewRound(currentPlayer.getUser().getUsername()));
        for (var gamePlayer: game.getPlayersList())
            eventPublisher.publishToPlayerInTheLobby(gameId, gamePlayer.getUser().getUsername(), new MyCardsState(gamePlayer));
    }

    private void handleNewTurn(Game game, GamePlayer winningPlayer) {
        game.setNewOrderAfterTurnEnd(winningPlayer);
        sendNewTurnMessages(game.getId(), winningPlayer.getUser().getUsername());
    }

    private void sendNewTurnMessages(Long gameId, String winningPlayerName) {
        eventPublisher.publishToLobby(
                gameId,
                List.of(new NewTurn(), new NextPlayerState(winningPlayerName, true))
        );
    }
}

package com.marafone.ai;

import com.marafone.marafone.game.model.*;
import java.io.Serializable;
import java.util.*;

public class MarafoneAI implements Serializable {

    private static final int SIMULATION_COUNT = 500;
    private final List<Card> allCardsReference;

    public MarafoneAI(List<Card> allCards) {
        this.allCardsReference = new ArrayList<>(allCards);
    }

    // Metody dla kompatybilności (nieużywane w MCTS)
    public static MarafoneAI load(String path) { return null; }
    public void save(String path) {}

    public Move selectMove(Game realGame, GamePlayer aiPlayer) {
        List<Move> validMoves = GameRules.getValidMoves(realGame, aiPlayer);

        if (validMoves.size() == 1) return validMoves.get(0);
        if (validMoves.get(0).getCard() == null) {
            return validMoves.get(new Random().nextInt(validMoves.size()));
        }

        Map<Move, Integer> winsPerMove = new HashMap<>();
        for (Move move : validMoves) {
            winsPerMove.put(move, 0);
        }

        System.out.println("AI (" + aiPlayer.getUser().getUsername() + ") analizuje " + validMoves.size() + " ruchów...");

        for (int i = 0; i < SIMULATION_COUNT; i++) {
            Game simulatedGame = createDeterminizedGame(realGame, aiPlayer);
            GamePlayer simAiPlayer = simulatedGame.findGamePlayerByUsername(aiPlayer.getUser().getUsername());

            Move moveToString = validMoves.get(i % validMoves.size());
            Move simMove = mapMoveToSim(moveToString, simulatedGame);

            applyMoveInternal(simulatedGame, simAiPlayer, simMove);

            Team winner = runRandomPlayout(simulatedGame);

            if (winner == aiPlayer.getTeam()) {
                winsPerMove.put(moveToString, winsPerMove.get(moveToString) + 1);
            }
        }

        Move bestMove = winsPerMove.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow()
                .getKey();

        System.out.println("=== WYNIKI ===");
        winsPerMove.forEach((m, w) -> System.out.println(
                (m.getCard() != null ? m.getCard().getRank() + " " + m.getCard().getSuit() : "ATUT")
                        + ": " + w + "/" + SIMULATION_COUNT));

        return bestMove;
    }

    private Team runRandomPlayout(Game game) {
        Random rand = new Random();
        int safetyCounter = 0;

        // Pętla symulacji całej rozgrywki
        while (!game.isEnded() && safetyCounter++ < 400) {

            // 1. Sprawdź czy lewa się skończyła (wszyscy zagrali)
            if (game.turnHasEnded()) {
                handleTrickEnd(game);

                // Sprawdź czy runda (10 lew) się skończyła
                if (game.roundHasEnded()) {
                    handleRoundEnd(game);
                    if (game.isEnded()) break; // Koniec gry (punkty osiągnięte)
                }
            }

            // 2. Pobierz aktualnego gracza
            GamePlayer current = game.getCurrentPlayerWithoutIterating();

            // Zabezpieczenie przed NPE (jeśli logika wyżej zawiedzie)
            if (current == null) {
                break;
            }

            // 3. Pobierz ruchy i graj losowo
            List<Move> moves = GameRules.getValidMoves(game, current);
            if (moves.isEmpty()) break;

            Move randomMove = moves.get(rand.nextInt(moves.size()));
            applyMoveInternal(game, current, randomMove);
        }

        return game.getWinnerTeam();
    }

    private void applyMoveInternal(Game game, GamePlayer player, Move move) {
        Round round = game.getRounds().getLast();
        if (move.getSuit() != null) {
            round.setTrumpSuit(move.getSuit());
            return;
        }

        Action action = Action.builder().player(player).card(move.getCard()).round(round).build();
        round.addNewAction(action);
        game.setLeadingSuitIfUnset(move.getCard().getSuit());
        player.removeCard(move.getCard());

        // Przesuń iterator na kolejnego gracza
        if (game.getCurrentPlayer().hasNext()) {
            game.getCurrentPlayer().next();
        }
    }

    // TO BYŁA BRAKUJĄCA LOGIKA:
    private void handleTrickEnd(Game game) {
        Round round = game.getRounds().getLast();
        List<Action> actions = round.getActions();

        // Pobierz ostatnie 4 akcje (trick)
        List<Action> trickActions = actions;
        if (actions.size() >= 4) {
            trickActions = actions.subList(actions.size() - 4, actions.size());
        }

        // Kto wygrał lewę?
        GamePlayer winner = GameRules.getTrickWinner(trickActions, round.getTrumpSuit());
        int points = GameRules.calculatePoints(trickActions);

        // Dodaj punkty w symulacji
        winner.addPoints(points);

        // Resetuj stan stołu
        game.setLeadingSuit(null);

        // KLUCZOWE: Ustaw zwycięzcę jako pierwszego w nowej lewie (to resetuje iterator!)
        game.setNewOrderAfterTurnEnd(winner);
    }

    private void handleRoundEnd(Game game) {
        // W symulacji MCTS zazwyczaj wystarczy sprawdzić wynik na koniec ręki,
        // bo rzadko symulujemy wiele rozdań (hands) w głąb.
        // Po prostu ustalamy zwycięzcę na podstawie obecnych punktów.

        int redPoints = game.getTeamPoints(Team.RED);
        int bluePoints = game.getTeamPoints(Team.BLUE);

        // Jeśli nikt nie osiągnął progu wygranej w tej ręce,
        // decydujemy na podstawie tego kto ma więcej punktów TERAZ.
        if (redPoints > bluePoints) {
            game.setWinnerTeam(Team.RED);
        } else {
            game.setWinnerTeam(Team.BLUE);
        }
    }

    // --- KLONOWANIE (Bez zmian, ale dla pewności wklejam całość) ---
    private Game createDeterminizedGame(Game realGame, GamePlayer perspectivePlayer) {
        Game clone = new Game();
        clone.setId(realGame.getId());
        clone.setPointsToWinGame(realGame.getPointsToWinGame());
        clone.setLeadingSuit(realGame.getLeadingSuit());

        List<Round> clonedRounds = new LinkedList<>();
        Round realLastRound = realGame.getRounds().getLast();
        Round clonedLastRound = new Round();
        clonedLastRound.setTrumpSuit(realLastRound.getTrumpSuit());
        clonedLastRound.setActions(new ArrayList<>(realLastRound.getActions()));
        clonedRounds.add(clonedLastRound);
        clone.setRounds(clonedRounds);

        List<GamePlayer> clonedPlayers = new ArrayList<>();
        List<Card> knownCards = new ArrayList<>();
        knownCards.addAll(perspectivePlayer.getOwnedCards());
        for(Action a : realLastRound.getActions()) knownCards.add(a.getCard());

        List<Card> unknownCards = new ArrayList<>(allCardsReference);
        unknownCards.removeAll(knownCards);
        Collections.shuffle(unknownCards);

        Iterator<Card> deckIterator = unknownCards.iterator();

        for (GamePlayer realP : realGame.getPlayersList()) {
            GamePlayer pClone = new GamePlayer();
            pClone.setUser(realP.getUser());
            pClone.setTeam(realP.getTeam());
            pClone.setPoints(realP.getPoints());
            pClone.setOwnedCards(new LinkedList<>());

            if (realP.getUser().getUsername().equals(perspectivePlayer.getUser().getUsername())) {
                pClone.getOwnedCards().addAll(perspectivePlayer.getOwnedCards());
            } else {
                int cardCount = realP.getOwnedCards().size();
                for (int i=0; i<cardCount; i++) {
                    if (deckIterator.hasNext()) {
                        pClone.getOwnedCards().add(deckIterator.next());
                    }
                }
            }
            clonedPlayers.add(pClone);
        }

        clone.setPlayersList(clonedPlayers);

        // Napraw referencje graczy w akcjach
        for(Action a : clonedLastRound.getActions()) {
            String uname = a.getPlayer().getUser().getUsername();
            a.setPlayer(clonedPlayers.stream().filter(p->p.getUser().getUsername().equals(uname)).findFirst().orElse(null));
        }

        // Ustaw iterator
        int currentIndex = realGame.getCurrentPlayerIndex();
        clone.setCurrentPlayer(clone.getPlayersList().listIterator(currentIndex));

        return clone;
    }

    private Move mapMoveToSim(Move realMove, Game simGame) {
        if (realMove.getCard() == null) return realMove;
        Card simCard = simGame.getCurrentPlayerWithoutIterating().getOwnedCards().stream()
                .filter(c -> c.getId().equals(realMove.getCard().getId()))
                .findFirst().orElse(null);
        return new Move(simCard, realMove.getSuit());
    }
}
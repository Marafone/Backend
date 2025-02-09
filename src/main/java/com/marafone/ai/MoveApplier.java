package com.marafone.ai;

import com.marafone.marafone.game.active.ActiveGameService;
import com.marafone.marafone.game.active.ActiveGameServiceImpl;
import com.marafone.marafone.game.model.Game;
import com.marafone.marafone.game.model.GamePlayer;
import com.marafone.marafone.game.event.incoming.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.marafone.ai.TrainingLoop.getValidMoves;

public class MoveApplier {

    public static ResponseEntity<String> applyMove(Game game, GamePlayer player, Move move, ActiveGameService gameService) {
        Long gameId = game.getId();
        String playerName = player.getUser().getUsername();

        if (move.getCard() == null && move.getSuit() != null) {
            // Selecting the trump suit
            TrumpSuitSelectEvent selectEvent = new TrumpSuitSelectEvent(move.getSuit());
            gameService.selectSuit(gameId, selectEvent, playerName);

            // After selecting the trump suit, play a card
            List<Move> validMoves = getValidMoves(game, player);
            if (!validMoves.isEmpty()) {
                Move cardMove = validMoves.get(0);
                if (cardMove.getCard() != null) {
                    CardSelectEvent cardEvent = new CardSelectEvent(cardMove.getCard().getId());
                    gameService.selectCard(gameId, cardEvent, playerName);
                }
            }
            return ResponseEntity.badRequest().body("No valid card move after selecting trump suit");
        } else if (move.getCard() != null) {
            // Playing a card
            CardSelectEvent cardEvent = new CardSelectEvent(move.getCard().getId());
            gameService.selectCard(gameId, cardEvent, playerName);
        }

        return ResponseEntity.badRequest().body("Invalid move");
    }
}

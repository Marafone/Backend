package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Action;
import com.marafone.marafone.game.model.Card;
import com.marafone.marafone.game.model.Game;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public final class TurnState extends OutEvent{

    public Map<String, Card> turn; //username -> card

    public TurnState(Game game){
        super("TurnState");

        turn = new HashMap<>();

        if(game.getRounds().isEmpty()){
            for(var gamePlayer: game.getPlayersList()){
                turn.put(gamePlayer.getUser().getUsername(), null);
            }
        }else{
            List<Action> actions = game.getRounds().getLast().getActions();
            ListIterator<Action> actionsBackIterator = actions.listIterator(actions.size());

            int properActions = actions.isEmpty() ? 0 : (actions.size() - 1) % 4 + 1;
            for(int i = 0; i < properActions; i++){
                Action action = actionsBackIterator.previous();
                turn.put(action.getPlayer().getUser().getUsername(), action.getCard());
            }

            for(var gamePlayer: game.getPlayersList()){
                turn.putIfAbsent(gamePlayer.getUser().getUsername(), null);
            }
        }
    }
}

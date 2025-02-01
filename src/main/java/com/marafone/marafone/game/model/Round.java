package com.marafone.marafone.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Round {
    @Id
    @GeneratedValue
    private Long id;
    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL)
    private List<Action> actions;
    @Enumerated(EnumType.STRING)
    private Suit trumpSuit;

    public List<Action> getLastNActions(int N){
        List<Action> currentTurn = new LinkedList<>();
        Iterator<Action> actionDescIterator = actions.reversed().iterator();
        for(int i = 0; i < N; i++){
            currentTurn.addFirst(actionDescIterator.next());
        }
        return currentTurn;
    }

    public boolean isTrumpSuitSelected(){
        return trumpSuit != null;
    }
}

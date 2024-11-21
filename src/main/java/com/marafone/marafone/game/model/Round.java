package com.marafone.marafone.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}

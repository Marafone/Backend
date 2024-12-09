package com.marafone.marafone.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Action {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn
    private GamePlayer player;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn
    @JsonIgnore
    private Round round;
    @ManyToOne
    @JoinColumn
    private Card card;
    private LocalDateTime timestamp;

    @Override
    public String toString() {
        return "Action id: " + id;
    }
}

package com.marafone.marafone.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Action implements Serializable {
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

    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode()
                : Objects.hash(player, card, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Action other = (Action) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        // Fallback if not persisted
        return player.equals(other.player) &&
                card.equals(other.card) &&
                timestamp.equals(other.timestamp);
    }
}

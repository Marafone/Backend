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

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Action implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(cascade = CascadeTy2pe.ALL)
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
        return (id != null) ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Action other = (Action) obj;
        return id != null && id.equals(other.id);
    }
}

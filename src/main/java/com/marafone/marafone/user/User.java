package com.marafone.marafone.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Entity
@Data
@Builder
@AllArgsConstructor
@Table(name = "users")
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String email;
    private int wins;
    private int losses;
    private boolean isInGame;
    @JsonIgnore
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
    public void increaseWins() {
        wins++;
    }
    public void increaseLosses() {
        losses++;
    }

    /*
        isInGame value cannot be trusted when set to true, as server can crash during game
         and game end without isInGame value being set back to false, thus this method accepts
          more time-consuming function that checks all active games
     */
    public boolean isInGame(Function<User,Boolean> checkUserInAllGames){
        if(!isInGame)
            return false;

        if(!checkUserInAllGames.apply(this)){
            isInGame = false;
        }

        return isInGame;
    }
}

package com.marafone.marafone.game.logic;

import com.marafone.marafone.game.model.Game;

public abstract class GameLogic {
    Game game;

    public abstract void addPlayer();
    public abstract void startGame();
    public abstract void checkTimeout();
    public abstract void selectCard();
    public abstract void selectSuit();
}

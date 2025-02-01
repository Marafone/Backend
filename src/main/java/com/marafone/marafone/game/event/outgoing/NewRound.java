package com.marafone.marafone.game.event.outgoing;

public final class NewRound extends OutEvent {
    public String firstPlayerName;

    public NewRound(String firstPlayerName){
        super("NewRound");
        this.firstPlayerName = firstPlayerName;
    }

}

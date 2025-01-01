package com.marafone.marafone.game.event.outgoing;

public final class ErrorEvent extends OutEvent {
    public String errorMessage;

    public ErrorEvent(String errorMessage) {
        super("ErrorEvent");
        this.errorMessage = errorMessage;
    }
}

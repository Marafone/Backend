package com.marafone.marafone.game.event.outgoing;

public final class ErrorEvent extends OutEvent {
    private String errorMessage;

    public ErrorEvent(String errorMessage) {
        super("ErrorEvent");
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

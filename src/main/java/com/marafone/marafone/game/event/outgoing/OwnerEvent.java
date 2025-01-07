package com.marafone.marafone.game.event.outgoing;

public final class OwnerEvent extends OutEvent {
    public String newOwnerName;
    public boolean isNew; // indicates whether a new owner has been selected or if it's the first owner

    public OwnerEvent(String newOwnerName, boolean isNew) {
        super("OwnerEvent");
        this.newOwnerName = newOwnerName;
        this.isNew = isNew;
    }
}

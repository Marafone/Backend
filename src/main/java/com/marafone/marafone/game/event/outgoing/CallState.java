package com.marafone.marafone.game.event.outgoing;

import com.marafone.marafone.game.model.Call;

public final class CallState extends OutEvent {

    public Call call;

    public CallState(Call call) {
        super("CallState");
        this.call = call;
    }
}

package com.marafone.marafone.game.event.outgoing;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
@JsonTypeInfo(use=JsonTypeInfo.Id.SIMPLE_NAME, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class OutEvent {
}

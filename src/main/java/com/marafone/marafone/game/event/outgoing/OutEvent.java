package com.marafone.marafone.game.event.outgoing;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
@JsonTypeInfo(use=JsonTypeInfo.Id.SIMPLE_NAME, include=JsonTypeInfo.As.PROPERTY, property="@class")
@Data
public class OutEvent {
}

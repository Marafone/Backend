package com.marafone.marafone.game.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameActionResponse {
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("message")
    String message;
}

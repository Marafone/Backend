package com.marafone.marafone.game.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.marafone.marafone.game.model.JoinGameResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinGameResponse {
    @JsonProperty("code")
    JoinGameResult code;
    @JsonProperty("message")
    String message;
}

package com.marafone.marafone.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SelectCardErrorMessages {
    NOT_YOUR_TURN("Not your turn! %s is deciding their move. Get ready for your next turn."),
    CARD_NOT_IN_HAND("The card you tried to play is not in your hand. Please select a valid card."),
    TRUMP_SUIT_NOT_SELECTED("You cannot play a card until the trump suit is selected."),
    INVALID_LEADING_SUIT_PLAY("Selected card is not part of the leading suit. Please play a valid card from the leading suit, %s.");

    private final String message;

    public String formatMessage(Object... args) {
        return String.format(message, args);
    }
}

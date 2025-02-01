package com.marafone.marafone.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChangeTeamErrorMessages {
    TEAM_IS_FULL("The team is already full. You cannot join this team at the moment."),
    SAME_TEAM("You are already a member of this team.");

    private final String message;
}

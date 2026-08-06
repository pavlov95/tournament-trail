package com.tournamenttrail.results.exceptions;

import java.util.UUID;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(UUID gameId) {
        super("Game with id " + gameId + " was not found.");
    }
}
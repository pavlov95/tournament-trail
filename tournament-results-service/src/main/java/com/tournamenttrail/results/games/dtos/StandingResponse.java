package com.tournamenttrail.results.games.dtos;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class StandingResponse {

    private UUID playerId;

    private String username;

    private BigDecimal points;

    private int gamesPlayed;

    private int wins;

    private int draws;

    private int losses;
}
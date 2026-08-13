package com.tournamenttrail.results.fixtures;

import com.tournamenttrail.results.games.Game;
import com.tournamenttrail.results.games.dtos.StandingResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class StandingResponseFixture {

    public static StandingResponse createLoserStanding(Game game){
        return StandingResponse.builder()
                .playerId(game.getWhitePlayerId())
                .username(game.getWhitePlayerUsername())
                .points(BigDecimal.ZERO)
                .gamesPlayed(1)
                .wins(0)
                .draws(0)
                .losses(1)
                .build();
    }

    public static StandingResponse createWinnerStanding(Game game){
        return StandingResponse.builder()
                .playerId(game.getBlackPlayerId())
                .username(game.getBlackPlayerUsername())
                .points(BigDecimal.ONE)
                .gamesPlayed(1)
                .wins(1)
                .draws(0)
                .losses(0)
                .build();
    }

    public static List<StandingResponse> createList(Game game){
        return List.of(createWinnerStanding(game), createLoserStanding(game));
    }


}

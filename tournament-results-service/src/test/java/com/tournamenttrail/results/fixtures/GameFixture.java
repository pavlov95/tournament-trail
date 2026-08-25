package com.tournamenttrail.results.fixtures;

import com.tournamenttrail.results.games.Game;
import com.tournamenttrail.results.games.enums.GameResult;
import com.tournamenttrail.results.games.enums.WinCondition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class GameFixture {
    public static int TEST_ROUND_NUMBER = 1;
    public static int TEST_BOARD_NUMBER = 1;
    public static int TEST_TOTAL_MOVES = 42;
    public static String TEST_WHITE_PLAYER_USERNAME = "TEST WHITE PLAYER";
    public static String TEST_BLACK_PLAYER_USERNAME = "TEST BLACK PLAYER";
    public static String TEST_ORGANISER_NOTE = "TEST ORGANISER NOTE";

    public static Game create() {
        LocalDateTime now = LocalDateTime.now();
        return Game.builder()
                .roundNumber(TEST_ROUND_NUMBER)
                .boardNumber(TEST_BOARD_NUMBER)
                .tournamentId(UUID.randomUUID())
                .whitePlayerId(UUID.randomUUID())
                .blackPlayerId(UUID.randomUUID())
                .whitePlayerUsername(TEST_WHITE_PLAYER_USERNAME)
                .blackPlayerUsername(TEST_BLACK_PLAYER_USERNAME)
                .result(GameResult.BLACK_WIN)
                .totalMoves(TEST_TOTAL_MOVES)
                .winCondition(WinCondition.RESIGNATION)
                .organiserNotes(TEST_ORGANISER_NOTE)
                .playedOn(now.minusDays(1))
                .whitePoints(BigDecimal.ZERO)
                .blackPoints(BigDecimal.ONE)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public static Game create(UUID tournamentId, int roundNumber, int boardNumber) {
        LocalDateTime now = LocalDateTime.now();
        return Game.builder()
                .roundNumber(roundNumber)
                .boardNumber(boardNumber)
                .tournamentId(tournamentId)
                .whitePlayerId(UUID.randomUUID())
                .blackPlayerId(UUID.randomUUID())
                .whitePlayerUsername(TEST_WHITE_PLAYER_USERNAME)
                .blackPlayerUsername(TEST_BLACK_PLAYER_USERNAME)
                .result(GameResult.BLACK_WIN)
                .totalMoves(TEST_TOTAL_MOVES)
                .winCondition(WinCondition.RESIGNATION)
                .organiserNotes(TEST_ORGANISER_NOTE)
                .playedOn(now.minusDays(1))
                .whitePoints(BigDecimal.ZERO)
                .blackPoints(BigDecimal.ONE)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }



}

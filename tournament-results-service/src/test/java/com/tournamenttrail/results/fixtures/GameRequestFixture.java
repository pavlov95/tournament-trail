package com.tournamenttrail.results.fixtures;

import com.tournamenttrail.results.games.dtos.GameRequest;
import com.tournamenttrail.results.games.enums.GameResult;
import com.tournamenttrail.results.games.enums.WinCondition;

import java.time.LocalDateTime;
import java.util.UUID;

public class GameRequestFixture {
    public static int TEST_ROUND_NUMBER = 1;
    public static int TEST_BOARD_NUMBER = 1;
    public static int TEST_TOTAL_MOVES = 42;
    public static String TEST_WHITE_PLAYER_USERNAME = "TEST WHITE PLAYER";
    public static String TEST_BLACK_PLAYER_USERNAME = "TEST BLACK PLAYER";
    public static String TEST_ORGANISER_NOTE = "TEST ORGANISER NOTE";

    public static int TEST_INVALID_ROUND_NUMBER = -1;
    public static int TEST_INVALID_BOARD_NUMBER = -11;
    public static int TEST_INVALID_TOTAL_MOVES = -42;

    public static int TEST_UPDATED_ROUND_NUMBER = 2;
    public static int TEST_UPDATED_BOARD_NUMBER = 2;
    public static int TEST_UPDATED_TOTAL_MOVES = 43;
    public static String TEST_UPDATED_WHITE_PLAYER_USERNAME = "TEST UPDATE WHITE PLAYER";
    public static String TEST_UPDATED_BLACK_PLAYER_USERNAME = "TEST UPDATED BLACK PLAYER";
    public static String TEST_UPDATED_ORGANISER_NOTE = "TEST UPDATED ORGANISER NOTE";


    public static GameRequest create() {
        GameRequest gameRequest = new GameRequest();
        gameRequest.setRoundNumber(TEST_ROUND_NUMBER);
        gameRequest.setBoardNumber(TEST_BOARD_NUMBER);
        gameRequest.setTotalMoves(TEST_TOTAL_MOVES);
        gameRequest.setWhitePlayerUsername(TEST_WHITE_PLAYER_USERNAME);
        gameRequest.setBlackPlayerUsername(TEST_BLACK_PLAYER_USERNAME);
        gameRequest.setWhitePlayerId(UUID.randomUUID());
        gameRequest.setBlackPlayerId(UUID.randomUUID());
        gameRequest.setResult(GameResult.BLACK_WIN);
        gameRequest.setWinCondition(WinCondition.RESIGNATION);
        gameRequest.setOrganiserNotes(TEST_ORGANISER_NOTE);
        gameRequest.setPlayedOn(LocalDateTime.now());

        return gameRequest;
    }

    public static GameRequest createInvalid(){
        GameRequest gameRequest = new GameRequest();
        gameRequest.setRoundNumber(TEST_INVALID_ROUND_NUMBER);
        gameRequest.setBoardNumber(TEST_INVALID_BOARD_NUMBER);
        gameRequest.setTotalMoves(TEST_INVALID_TOTAL_MOVES);
        gameRequest.setWhitePlayerUsername(null);
        gameRequest.setBlackPlayerUsername(null);
        gameRequest.setWhitePlayerId(UUID.randomUUID());
        gameRequest.setBlackPlayerId(UUID.randomUUID());
        gameRequest.setResult(GameResult.BLACK_WIN);
        gameRequest.setWinCondition(WinCondition.RESIGNATION);
        gameRequest.setOrganiserNotes(TEST_ORGANISER_NOTE);
        gameRequest.setPlayedOn(LocalDateTime.now());

        return gameRequest;
    }

    public static GameRequest createUpdated(){
        GameRequest gameRequest = new GameRequest();
        gameRequest.setRoundNumber(TEST_UPDATED_ROUND_NUMBER);
        gameRequest.setBoardNumber(TEST_UPDATED_BOARD_NUMBER);
        gameRequest.setTotalMoves(TEST_UPDATED_TOTAL_MOVES);
        gameRequest.setWhitePlayerUsername(TEST_UPDATED_WHITE_PLAYER_USERNAME);
        gameRequest.setBlackPlayerUsername(TEST_UPDATED_BLACK_PLAYER_USERNAME);
        gameRequest.setWhitePlayerId(UUID.randomUUID());
        gameRequest.setBlackPlayerId(UUID.randomUUID());
        gameRequest.setResult(GameResult.WHITE_WIN);
        gameRequest.setWinCondition(WinCondition.CHECKMATE);
        gameRequest.setOrganiserNotes(TEST_UPDATED_ORGANISER_NOTE);
        gameRequest.setPlayedOn(LocalDateTime.now());

        return gameRequest;
        }


}
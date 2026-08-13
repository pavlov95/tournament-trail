package com.tournamenttrail.results.games;

import com.tournamenttrail.results.fixtures.GameFixture;
import com.tournamenttrail.results.fixtures.GameRequestFixture;
import com.tournamenttrail.results.games.dtos.GameRequest;
import com.tournamenttrail.results.games.dtos.GameResponse;
import com.tournamenttrail.results.games.dtos.StandingResponse;
import com.tournamenttrail.results.games.enums.GameResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {
    @Mock
    private GameRepository gameRepository;

    @Captor
    ArgumentCaptor<Game> captor;

    @InjectMocks
    private GameService gameService;

    @Test
    public void createGame_shouldCreateGameAndSaveIt(){
        Game game = GameFixture.create();
        UUID tournamentId = game.getTournamentId();
        GameRequest gameRequest = GameRequestFixture.create();

        LocalDateTime before = LocalDateTime.now();
        gameService.createGame(tournamentId, gameRequest);

        verify(gameRepository).save(captor.capture());

        Game savedGame = captor.getValue();

        assertEquals(GameRequestFixture.TEST_BOARD_NUMBER, savedGame.getBoardNumber());
        assertEquals(GameRequestFixture.TEST_ROUND_NUMBER, savedGame.getRoundNumber());
        assertEquals(GameRequestFixture.TEST_TOTAL_MOVES, savedGame.getTotalMoves());
        assertEquals(tournamentId, savedGame.getTournamentId());
        assertEquals(gameRequest.getWhitePlayerId(), savedGame.getWhitePlayerId());
        assertEquals(gameRequest.getBlackPlayerId(), savedGame.getBlackPlayerId());
        assertEquals(GameRequestFixture.TEST_WHITE_PLAYER_USERNAME, savedGame.getWhitePlayerUsername());
        assertEquals(GameRequestFixture.TEST_BLACK_PLAYER_USERNAME, savedGame.getBlackPlayerUsername());
        assertEquals(gameRequest.getResult(), savedGame.getResult());
        assertEquals(gameRequest.getWinCondition(), savedGame.getWinCondition());
        assertEquals(GameRequestFixture.TEST_ORGANISER_NOTE, savedGame.getOrganiserNotes());
        assertEquals(gameRequest.getPlayedOn(), savedGame.getPlayedOn());
        assertFalse(before.isBefore(savedGame.getCreatedOn()));
        assertFalse(before.isBefore(savedGame.getUpdatedOn()));
        assertEquals(BigDecimal.ONE, savedGame.getBlackPoints());
        assertEquals(BigDecimal.ZERO, savedGame.getWhitePoints());
    }

    @Test
    public void deleteGame_shouldDeleteGameFromDatabase(){
        Game game = GameFixture.create();
        when(gameRepository.findByIdAndTournamentId(game.getId(), game.getTournamentId()))
                .thenReturn(Optional.of(game));
        gameService.deleteGame(game.getTournamentId(), game.getId());

        verify(gameRepository).delete(game);
    }

    @Test
    public void updateGame_shouldUpdateGame(){
        Game game = GameFixture.create();
        when(gameRepository.findByIdAndTournamentId(game.getId(), game.getTournamentId()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameRequest gameRequest = GameRequestFixture.create();
        LocalDateTime before = LocalDateTime.now();
        gameService.updateGame(game.getTournamentId(), game.getId(), gameRequest);

        verify(gameRepository).save(captor.capture());
        Game savedGame = captor.getValue();

        assertEquals(GameRequestFixture.TEST_BOARD_NUMBER, savedGame.getBoardNumber());
        assertEquals(GameRequestFixture.TEST_ROUND_NUMBER, savedGame.getRoundNumber());
        assertEquals(GameRequestFixture.TEST_TOTAL_MOVES, savedGame.getTotalMoves());
        assertEquals(game.getTournamentId(), savedGame.getTournamentId());
        assertEquals(gameRequest.getWhitePlayerId(), savedGame.getWhitePlayerId());
        assertEquals(gameRequest.getBlackPlayerId(), savedGame.getBlackPlayerId());
        assertEquals(GameRequestFixture.TEST_WHITE_PLAYER_USERNAME, savedGame.getWhitePlayerUsername());
        assertEquals(GameRequestFixture.TEST_BLACK_PLAYER_USERNAME, savedGame.getBlackPlayerUsername());
        assertEquals(gameRequest.getResult(), savedGame.getResult());
        assertEquals(gameRequest.getWinCondition(), savedGame.getWinCondition());
        assertEquals(GameRequestFixture.TEST_ORGANISER_NOTE, savedGame.getOrganiserNotes());
        assertEquals(gameRequest.getPlayedOn(), savedGame.getPlayedOn());
        assertFalse(before.isBefore(savedGame.getCreatedOn()));
        assertTrue(before.isBefore(savedGame.getUpdatedOn()));
        assertEquals(BigDecimal.ONE, savedGame.getBlackPoints());
        assertEquals(BigDecimal.ZERO, savedGame.getWhitePoints());
    }


    @Test
    public void getGamesByTournament_shouldReturnEmptyListWhenNoGamesExist() {
        UUID tournamentId = UUID.randomUUID();

        when(gameRepository
                .findAllByTournamentIdOrderByRoundNumberAscBoardNumberAsc(tournamentId))
                .thenReturn(List.of());

        List<GameResponse> result = gameService.getGamesByTournament(tournamentId);

        assertTrue(result.isEmpty());

        verify(gameRepository)
                .findAllByTournamentIdOrderByRoundNumberAscBoardNumberAsc(tournamentId);
    }

    @Test
    public void getStandings_shouldAccumulateStatisticsAcrossMultipleGames() {
        UUID tournamentId = UUID.randomUUID();

        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        UUID charlieId = UUID.randomUUID();

        Game game1 = GameFixture.create();
        game1.setTournamentId(tournamentId);
        game1.setWhitePlayerId(aliceId);
        game1.setWhitePlayerUsername("Alice");
        game1.setBlackPlayerId(bobId);
        game1.setBlackPlayerUsername("Bob");
        game1.setResult(GameResult.WHITE_WIN);

        Game game2 = GameFixture.create();
        game2.setTournamentId(tournamentId);
        game2.setWhitePlayerId(aliceId);
        game2.setWhitePlayerUsername("Alice");
        game2.setBlackPlayerId(charlieId);
        game2.setBlackPlayerUsername("Charlie");
        game2.setResult(GameResult.DRAW);

        Game game3 = GameFixture.create();
        game3.setTournamentId(tournamentId);
        game3.setWhitePlayerId(bobId);
        game3.setWhitePlayerUsername("Bob");
        game3.setBlackPlayerId(charlieId);
        game3.setBlackPlayerUsername("Charlie");
        game3.setResult(GameResult.BLACK_WIN);

        when(gameRepository
                .findAllByTournamentIdOrderByRoundNumberAscBoardNumberAsc(tournamentId))
                .thenReturn(List.of(game1, game2, game3));

        List<StandingResponse> standings = gameService.getStandings(tournamentId);

        assertEquals(3, standings.size());

        StandingResponse alice = standings.get(0);
        StandingResponse charlie = standings.get(1);
        StandingResponse bob = standings.get(2);

        assertEquals("Alice", alice.getUsername());
        assertEquals(new BigDecimal("1.5"), alice.getPoints());
        assertEquals(2, alice.getGamesPlayed());
        assertEquals(1, alice.getWins());
        assertEquals(1, alice.getDraws());
        assertEquals(0, alice.getLosses());

        assertEquals("Charlie", charlie.getUsername());
        assertEquals(new BigDecimal("1.5"), charlie.getPoints());
        assertEquals(2, charlie.getGamesPlayed());
        assertEquals(1, charlie.getWins());
        assertEquals(1, charlie.getDraws());
        assertEquals(0, charlie.getLosses());

        assertEquals("Bob", bob.getUsername());
        assertEquals(BigDecimal.ZERO, bob.getPoints());
        assertEquals(2, bob.getGamesPlayed());
        assertEquals(0, bob.getWins());
        assertEquals(0, bob.getDraws());
        assertEquals(2, bob.getLosses());
    }
}

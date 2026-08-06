package com.tournamenttrail.results.games;

import com.tournamenttrail.results.exceptions.GameNotFoundException;
import com.tournamenttrail.results.games.dtos.GameRequest;
import com.tournamenttrail.results.games.dtos.GameResponse;
import com.tournamenttrail.results.games.dtos.StandingResponse;
import com.tournamenttrail.results.games.enums.GameResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private static final BigDecimal WIN_POINTS = BigDecimal.ONE;
    private static final BigDecimal DRAW_POINTS = new BigDecimal("0.5");
    private static final BigDecimal LOSS_POINTS = BigDecimal.ZERO;

    private final GameRepository gameRepository;

    @Transactional
    public GameResponse createGame(UUID tournamentId, GameRequest gameRequest) {
        Game game = new Game();

        game.setTournamentId(tournamentId);

        applyRequestData(game, gameRequest);
        applyPoints(game);

        LocalDateTime now = LocalDateTime.now();
        game.setCreatedOn(now);
        game.setUpdatedOn(now);

        Game savedGame = gameRepository.save(game);

        log.info("Created game result {} for tournament {}", savedGame.getId(), tournamentId);

        return mapToGameResponse(savedGame);
    }

    @Transactional
    public GameResponse updateGame(UUID tournamentId, UUID gameId, GameRequest gameRequest) {
        Game game = getGameOrThrow(tournamentId, gameId);
        game.setUpdatedOn(LocalDateTime.now());
        applyRequestData(game, gameRequest);
        applyPoints(game);

        Game updatedGame = gameRepository.save(game);

        log.info("Updated game result {} for tournament {}", gameId, tournamentId);

        return mapToGameResponse(updatedGame);
    }

    @Transactional
    public void deleteGame(UUID tournamentId, UUID gameId) {
        Game game = getGameOrThrow(tournamentId, gameId);

        gameRepository.delete(game);

        log.info("Deleted game result {} from tournament {}", gameId, tournamentId);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getGamesByTournament(UUID tournamentId) {
        return gameRepository.findAllByTournamentIdOrderByRoundNumberAscBoardNumberAsc(tournamentId)
                .stream()
                .map(this::mapToGameResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StandingResponse> getStandings(UUID tournamentId) {
        List<Game> games = gameRepository.findAllByTournamentIdOrderByRoundNumberAscBoardNumberAsc(tournamentId);

        Map<UUID, StandingStats> standings = new HashMap<>();

        for (Game game : games) {
            if (game.getResult() == GameResult.PENDING) {
                continue;
            }

            StandingStats whiteStats = standings.computeIfAbsent(
                    game.getWhitePlayerId(),
                    playerId -> new StandingStats(game.getWhitePlayerId(), game.getWhitePlayerUsername())
            );

            StandingStats blackStats = standings.computeIfAbsent(
                    game.getBlackPlayerId(),
                    playerId -> new StandingStats(game.getBlackPlayerId(), game.getBlackPlayerUsername())
            );

            applyGameToStandings(game, whiteStats, blackStats);
        }

        return standings.values()
                .stream()
                .sorted(
                        Comparator.comparing(StandingStats::getPoints).reversed()
                                .thenComparing(StandingStats::getUsername)
                )
                .map(this::mapToStandingResponse)
                .toList();
    }

    private Game getGameOrThrow(UUID tournamentId, UUID gameId) {
        return gameRepository.findByIdAndTournamentId(gameId, tournamentId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    private void applyRequestData(Game game, GameRequest gameRequest) {
        game.setRoundNumber(gameRequest.getRoundNumber());
        game.setBoardNumber(gameRequest.getBoardNumber());

        game.setWhitePlayerId(gameRequest.getWhitePlayerId());
        game.setWhitePlayerUsername(gameRequest.getWhitePlayerUsername());

        game.setBlackPlayerId(gameRequest.getBlackPlayerId());
        game.setBlackPlayerUsername(gameRequest.getBlackPlayerUsername());

        game.setResult(gameRequest.getResult());
        game.setTotalMoves(gameRequest.getTotalMoves());
        game.setWinCondition(gameRequest.getWinCondition());
        game.setOrganiserNotes(gameRequest.getOrganiserNotes());
        game.setPlayedOn(gameRequest.getPlayedOn());
    }

    private void applyPoints(Game game) {
        switch (game.getResult()) {
            case WHITE_WIN -> {
                game.setWhitePoints(WIN_POINTS);
                game.setBlackPoints(LOSS_POINTS);
            }
            case BLACK_WIN -> {
                game.setWhitePoints(LOSS_POINTS);
                game.setBlackPoints(WIN_POINTS);
            }
            case DRAW -> {
                game.setWhitePoints(DRAW_POINTS);
                game.setBlackPoints(DRAW_POINTS);
            }
            case PENDING -> {
                game.setWhitePoints(LOSS_POINTS);
                game.setBlackPoints(LOSS_POINTS);
            }
        }
    }

    private void applyGameToStandings(Game game, StandingStats whiteStats, StandingStats blackStats) {
        whiteStats.addGamePlayed();
        blackStats.addGamePlayed();

        switch (game.getResult()) {
            case WHITE_WIN -> {
                whiteStats.addWin();
                blackStats.addLoss();
            }
            case BLACK_WIN -> {
                whiteStats.addLoss();
                blackStats.addWin();
            }
            case DRAW -> {
                whiteStats.addDraw();
                blackStats.addDraw();
            }
            case PENDING -> {
            }
        }
    }

    private GameResponse mapToGameResponse(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .tournamentId(game.getTournamentId())
                .roundNumber(game.getRoundNumber())
                .boardNumber(game.getBoardNumber())
                .whitePlayerId(game.getWhitePlayerId())
                .whitePlayerUsername(game.getWhitePlayerUsername())
                .blackPlayerId(game.getBlackPlayerId())
                .blackPlayerUsername(game.getBlackPlayerUsername())
                .result(game.getResult())
                .whitePoints(game.getWhitePoints())
                .blackPoints(game.getBlackPoints())
                .totalMoves(game.getTotalMoves())
                .winCondition(game.getWinCondition())
                .organiserNotes(game.getOrganiserNotes())
                .playedOn(game.getPlayedOn())
                .createdOn(game.getCreatedOn())
                .updatedOn(game.getUpdatedOn())
                .build();
    }

    private StandingResponse mapToStandingResponse(StandingStats stats) {
        return StandingResponse.builder()
                .playerId(stats.getPlayerId())
                .username(stats.getUsername())
                .points(stats.getPoints())
                .gamesPlayed(stats.getGamesPlayed())
                .wins(stats.getWins())
                .draws(stats.getDraws())
                .losses(stats.getLosses())
                .build();
    }

    private static class StandingStats {

        private final UUID playerId;
        private final String username;
        private BigDecimal points;
        private int gamesPlayed;
        private int wins;
        private int draws;
        private int losses;

        private StandingStats(UUID playerId, String username) {
            this.playerId = playerId;
            this.username = username;
            this.points = BigDecimal.ZERO;
        }

        private UUID getPlayerId() {
            return playerId;
        }

        private String getUsername() {
            return username;
        }

        private BigDecimal getPoints() {
            return points;
        }

        private int getGamesPlayed() {
            return gamesPlayed;
        }

        private int getWins() {
            return wins;
        }

        private int getDraws() {
            return draws;
        }

        private int getLosses() {
            return losses;
        }

        private void addGamePlayed() {
            this.gamesPlayed++;
        }

        private void addWin() {
            this.wins++;
            this.points = this.points.add(WIN_POINTS);
        }

        private void addDraw() {
            this.draws++;
            this.points = this.points.add(DRAW_POINTS);
        }

        private void addLoss() {
            this.losses++;
        }
    }
}
package com.tournamenttrail.results.games;

import com.tournamenttrail.results.games.dtos.GameRequest;
import com.tournamenttrail.results.games.dtos.GameResponse;
import com.tournamenttrail.results.games.dtos.StandingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/games")
    public List<GameResponse> getGamesByTournament(@PathVariable UUID tournamentId) {
        return gameService.getGamesByTournament(tournamentId);
    }

    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse createGame(@PathVariable UUID tournamentId, @Valid @RequestBody GameRequest gameRequest) {

        return gameService.createGame(tournamentId, gameRequest);
    }

    @PutMapping("/games/{gameId}")
    public GameResponse updateGame(@PathVariable UUID tournamentId, @PathVariable UUID gameId
            , @Valid @RequestBody GameRequest gameRequest) {

        return gameService.updateGame(tournamentId, gameId, gameRequest);
    }

    @DeleteMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable UUID tournamentId, @PathVariable UUID gameId) {

        gameService.deleteGame(tournamentId, gameId);
    }

    @GetMapping("/standings")
    public List<StandingResponse> getStandings(@PathVariable UUID tournamentId) {

        return gameService.getStandings(tournamentId);
    }
}
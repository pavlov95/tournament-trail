package tournament_trail.demo.resultclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "tournament-results-service", url = "${tournament-results.service.url}")
public interface TournamentResultsClient {

    @GetMapping("/api/tournaments/{tournamentId}/games")
    List<GameResponse> getGamesByTournament(@PathVariable UUID tournamentId);

    @PostMapping("/api/tournaments/{tournamentId}/games")
    GameResponse createGame(@PathVariable UUID tournamentId, @RequestBody GameRequest gameRequest);

    @PutMapping("/api/tournaments/{tournamentId}/games/{gameId}")
    GameResponse updateGame(
            @PathVariable UUID tournamentId,
            @PathVariable UUID gameId,
            @RequestBody GameRequest gameRequest);

    @DeleteMapping("/api/tournaments/{tournamentId}/games/{gameId}")
    void deleteGame(@PathVariable UUID tournamentId, @PathVariable UUID gameId);


    @GetMapping("/api/tournaments/{tournamentId}/standings")
    List<StandingResponse> getStandings(@PathVariable UUID tournamentId);
}
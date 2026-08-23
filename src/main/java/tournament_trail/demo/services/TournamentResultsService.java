package tournament_trail.demo.services;

import org.springframework.stereotype.Service;
import tournament_trail.demo.resultclient.GameRequest;
import tournament_trail.demo.resultclient.GameResponse;
import tournament_trail.demo.resultclient.StandingResponse;
import tournament_trail.demo.resultclient.TournamentResultsClient;


import java.util.List;
import java.util.UUID;

@Service
public class TournamentResultsService {

    private final TournamentResultsClient tournamentResultsClient;

    public TournamentResultsService(TournamentResultsClient tournamentResultsClient) {
        this.tournamentResultsClient = tournamentResultsClient;
    }

    public GameResponse createGame(UUID tournamentId, GameRequest gameRequest) {
        return tournamentResultsClient.createGame(tournamentId, gameRequest);
    }

    public GameResponse updateGame(UUID tournamentId, UUID gameId, GameRequest gameRequest) {
        return tournamentResultsClient.updateGame(tournamentId, gameId, gameRequest);
    }

    public void deleteGame(UUID tournamentId, UUID gameId) {
        tournamentResultsClient.deleteGame(tournamentId, gameId);
    }

    public List<GameResponse> getGamesByTournament(UUID tournamentId) {
        return tournamentResultsClient.getGamesByTournament(tournamentId);
    }

    public List<StandingResponse> getStandings(UUID tournamentId) {
        return tournamentResultsClient.getStandings(tournamentId);
    }


}

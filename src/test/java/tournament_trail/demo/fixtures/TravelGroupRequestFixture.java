package tournament_trail.demo.fixtures;

import tournament_trail.demo.web.dtos.TravelGroupRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public class TravelGroupRequestFixture {
    public static TravelGroupRequest create(UUID id, UUID tournamentId){
        TravelGroupRequest travelGroupRequest = new TravelGroupRequest();
        travelGroupRequest.setTournamentId(id);
        travelGroupRequest.setTournamentId(tournamentId);
        travelGroupRequest.setDepartureTime(LocalDateTime.now().plusHours(1));
        return travelGroupRequest;
    }
}

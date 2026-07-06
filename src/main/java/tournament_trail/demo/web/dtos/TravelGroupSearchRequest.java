package tournament_trail.demo.web.dtos;

import lombok.Data;
import tournament_trail.demo.entities.enums.TransportationType;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import java.util.UUID;

@Data
public class TravelGroupSearchRequest {

    private String name;

    private UUID tournamentId;

    private String departureCountry;

    private String departureCity;

    private TransportationType transportationType;

    private Integer maximumMembers;

    private TravelGroupStatus status;

}

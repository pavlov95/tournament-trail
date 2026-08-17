package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TransportationType;
import tournament_trail.demo.web.dtos.TravelGroupRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TravelGroupRequestFixture {
    public final static String TEST_NAME = "Test name";
    public final static String TEST_DESCRIPTION = "Test description";
    public final static String TEST_CITY = "Test city";
    public final static String TEST_COUNTRY = "Test country";
    public final static String TEST_MEETING_POINT = "Test country";
    public final static CurrencyCode TEST_CURRENCY = CurrencyCode.EUR;
    public final static BigDecimal TEST_ESTIMATED_COST = BigDecimal.TEN;
    public final static TransportationType TEST_TRANSPORTATION_TYPE = TransportationType.CAR;
    public final static LocalDateTime TEST_DEPARTURE_DATE = LocalDateTime.now().plusDays(1);
    public final static int TEST_MAXIMUM_MEMBERS = 4;

    public static TravelGroupRequest create() {
        TravelGroupRequest travelGroupRequest = new TravelGroupRequest();
        travelGroupRequest.setTournamentId(UUID.randomUUID());
        travelGroupRequest.setDepartureTime(TEST_DEPARTURE_DATE);
        travelGroupRequest.setDescription(TEST_DESCRIPTION);
        travelGroupRequest.setName(TEST_NAME);
        travelGroupRequest.setDepartureCity(TEST_CITY);
        travelGroupRequest.setDepartureCountry(TEST_COUNTRY);
        travelGroupRequest.setMeetingPoint(TEST_MEETING_POINT);
        travelGroupRequest.setCurrency(TEST_CURRENCY);
        travelGroupRequest.setEstimatedCost(TEST_ESTIMATED_COST);
        travelGroupRequest.setTransportationType(TEST_TRANSPORTATION_TYPE);
        travelGroupRequest.setMaximumMembers(TEST_MAXIMUM_MEMBERS);

        return travelGroupRequest;
    }

    public static TravelGroupRequest createInvalid(){
        return new TravelGroupRequest();
    }

    public static TravelGroupRequest createWithTournament(UUID tournamentId) {
        TravelGroupRequest travelGroupRequest = new TravelGroupRequest();
        travelGroupRequest.setTournamentId(tournamentId);
        travelGroupRequest.setDepartureTime(TEST_DEPARTURE_DATE);
        travelGroupRequest.setDescription(TEST_DESCRIPTION);
        travelGroupRequest.setName(TEST_NAME);
        travelGroupRequest.setDepartureCity(TEST_CITY);
        travelGroupRequest.setDepartureCountry(TEST_COUNTRY);
        travelGroupRequest.setMeetingPoint(TEST_MEETING_POINT);
        travelGroupRequest.setCurrency(TEST_CURRENCY);
        travelGroupRequest.setEstimatedCost(TEST_ESTIMATED_COST);
        travelGroupRequest.setTransportationType(TEST_TRANSPORTATION_TYPE);
        travelGroupRequest.setMaximumMembers(TEST_MAXIMUM_MEMBERS);

        return travelGroupRequest;
    }
}

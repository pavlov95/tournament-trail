package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TransportationType;
import tournament_trail.demo.entities.enums.TravelGroupStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TravelGroupFixture {
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
    public static final LocalDateTime TEST_NOW = LocalDateTime.now();

    public static TravelGroup create() {
        return TravelGroup.builder()
                .id(UUID.randomUUID())
                .owner(UserFixture.createUser())
                .status(TravelGroupStatus.OPEN)
                .createdOn(TEST_NOW)
                .updatedOn(TEST_NOW)
                .name(TEST_NAME)
                .departureCity(TEST_CITY)
                .departureCountry(TEST_COUNTRY)
                .meetingPoint(TEST_MEETING_POINT)
                .description(TEST_DESCRIPTION)
                .currency(TEST_CURRENCY)
                .maximumMembers(TEST_MAXIMUM_MEMBERS)
                .tournament(TournamentFixture.create())
                .departureTime(TEST_DEPARTURE_DATE)
                .estimatedCost(TEST_ESTIMATED_COST)
                .transportationType(TEST_TRANSPORTATION_TYPE)
                .build();
    }

    public static TravelGroup createWithCancelledStatus() {
        return TravelGroup.builder()
                .id(UUID.randomUUID())
                .owner(UserFixture.createUser())
                .status(TravelGroupStatus.CANCELLED)
                .createdOn(TEST_NOW)
                .updatedOn(TEST_NOW)
                .name(TEST_NAME)
                .departureCity(TEST_CITY)
                .departureCountry(TEST_COUNTRY)
                .meetingPoint(TEST_MEETING_POINT)
                .description(TEST_DESCRIPTION)
                .currency(TEST_CURRENCY)
                .tournament(TournamentFixture.create())
                .departureTime(TEST_DEPARTURE_DATE)
                .maximumMembers(TEST_MAXIMUM_MEMBERS)
                .estimatedCost(TEST_ESTIMATED_COST)
                .transportationType(TEST_TRANSPORTATION_TYPE)
                .build();
    }

    public static List<TravelGroup> creteListWithSameOwner() {
        TravelGroup first = TravelGroupFixture.create();
        TravelGroup second = TravelGroupFixture.create();
        second.setOwner(first.getOwner());
        return List.of(first, second);
    }

    public static List<TravelGroup> createList(){
        TravelGroup first = TravelGroupFixture.create();
        TravelGroup second = TravelGroupFixture.create();
        return List.of(first, second);
    }

    public static TravelGroup createWithoutIdAndUserAndTournament(){
        return TravelGroup.builder()
                .status(TravelGroupStatus.OPEN)
                .createdOn(TEST_NOW)
                .updatedOn(TEST_NOW)
                .name(TEST_NAME)
                .departureCity(TEST_CITY)
                .departureCountry(TEST_COUNTRY)
                .meetingPoint(TEST_MEETING_POINT)
                .description(TEST_DESCRIPTION)
                .currency(TEST_CURRENCY)
                .maximumMembers(TEST_MAXIMUM_MEMBERS)
                .departureTime(TEST_DEPARTURE_DATE)
                .estimatedCost(TEST_ESTIMATED_COST)
                .transportationType(TEST_TRANSPORTATION_TYPE)
                .build();
    }
}

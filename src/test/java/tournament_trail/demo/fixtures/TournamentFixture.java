package tournament_trail.demo.fixtures;


import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TimeControl;
import tournament_trail.demo.entities.enums.TournamentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TournamentFixture {
    public static String TEST_CITY = "Test city";
    public static String TEST_NAME = "Test name";
    public static String TEST_COUNTRY = "Test country";
    public static String TEST_VENUE = "Test venue";
    public static String TEST_DESCRIPTION = "Test description";
    public static String TEST_PAYMENT_INSTRUCTIONS = "Test payment instructions";
    public static int TEST_MAXIMUM_PARTICIPANTS = 111;
    public static CurrencyCode TEST_CURRENCY = CurrencyCode.EUR;
    public static TimeControl TEST_TIME_CONTROL = TimeControl.BULLET;
    public static TournamentStatus TEST_TOURNAMENT_STATUS = TournamentStatus.PUBLISHED;

    public static Tournament createWithStatusStarted() {
        LocalDateTime now = LocalDateTime.now();
        return Tournament.builder()
                .id(UUID.randomUUID())
                .country(TEST_COUNTRY)
                .city(TEST_CITY)
                .venue(TEST_VENUE)
                .organiser(UserFixture.createUser())
                .registrationDeadline(now)
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .currency(TEST_CURRENCY)
                .description(TEST_DESCRIPTION)
                .rated(true)
                .createdOn(now)
                .entryFee(BigDecimal.TEN)
                .timeControl(TEST_TIME_CONTROL)
                .paymentInstructions(TEST_PAYMENT_INSTRUCTIONS)
                .status(TournamentStatus.STARTED)
                .build();
    }

    public static Tournament createWithStatusRegistrationClosed() {
        return Tournament.builder()
                .id(UUID.randomUUID())
                .status(TournamentStatus.REGISTRATION_CLOSED)
                .build();
    }

    public static Tournament createWithStatusCompleted() {
        return Tournament.builder()
                .organiser(UserFixture.createUser())
                .status(TournamentStatus.COMPLETED)
                .build();
    }

    public static Tournament create() {
        LocalDateTime now = LocalDateTime.now();
        return Tournament.builder()
                .id(UUID.randomUUID())
                .name(TEST_NAME)
                .country(TEST_COUNTRY)
                .city(TEST_CITY)
                .venue(TEST_VENUE)
                .organiser(UserFixture.createUser())
                .registrationDeadline(now)
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .currency(TEST_CURRENCY)
                .description(TEST_DESCRIPTION)
                .rated(true)
                .createdOn(now)
                .entryFee(BigDecimal.TEN)
                .timeControl(TEST_TIME_CONTROL)
                .paymentInstructions(TEST_PAYMENT_INSTRUCTIONS)
                .status(TEST_TOURNAMENT_STATUS)
                .build();
    }

    public static Tournament createWithStatus(TournamentStatus status) {
        return Tournament.builder()
                .id(UUID.randomUUID())
                .organiser(UserFixture.createUser())
                .city(TEST_CITY)
                .country(TEST_COUNTRY)
                .venue(TEST_VENUE)
                .status(status)
                .maximumParticipants(TEST_MAXIMUM_PARTICIPANTS)
                .description(TEST_DESCRIPTION)
                .startTime(LocalDateTime.now())
                .build();
    }

    public static Tournament createTournamentWithStartTime(LocalDateTime localDateTime) {
        return Tournament.builder()
                .id(UUID.randomUUID())
                .city(TEST_CITY)
                .country(TEST_COUNTRY)
                .venue(TEST_VENUE)
                .description(TEST_DESCRIPTION)
                .status(TEST_TOURNAMENT_STATUS)
                .startTime(localDateTime)
                .build();
    }

    public static Tournament createWithStatusCancelled() {
        return Tournament.builder()
                .id(UUID.randomUUID())
                .organiser(UserFixture.createUser())
                .status(TournamentStatus.CANCELLED)
                .build();
    }

    public static Tournament createWithStatusPublished() {
        return Tournament.builder()
                .id(UUID.randomUUID())
                .status(TournamentStatus.PUBLISHED)
                .entryFee(BigDecimal.ZERO)
                .build();
    }

    public static Tournament createPaidWithStatusPublished() {
        return Tournament.builder()
                .id(UUID.randomUUID())
                .status(TournamentStatus.PUBLISHED)
                .entryFee(BigDecimal.TEN)
                .build();
    }

    public static Tournament createPaidWithStatusDraft() {
        return Tournament.builder()
                .id(UUID.randomUUID())
                .organiser(UserFixture.createUser())
                .status(TournamentStatus.DRAFT)
                .entryFee(BigDecimal.TEN)
                .build();
    }

    public static List<Tournament> createList() {
        Tournament first = TournamentFixture.create();
        Tournament second = TournamentFixture.create();

        return List.of(first, second);
    }

    public static List<Tournament> createListWithPublishedStartedAndRegistrationClosedStatuses() {
        Tournament first = TournamentFixture.create();
        Tournament second = TournamentFixture.createWithStatusStarted();
        Tournament third = TournamentFixture.createWithStatusRegistrationClosed();

        return List.of(first, second, third);
    }

    public static Tournament createWithoutIdAndOrganiser(){
        LocalDateTime now = LocalDateTime.now();
        return Tournament.builder()
                .name(TEST_NAME)
                .country(TEST_COUNTRY)
                .city(TEST_CITY)
                .venue(TEST_VENUE)
                .registrationDeadline(now)
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .currency(TEST_CURRENCY)
                .description(TEST_DESCRIPTION)
                .rated(true)
                .createdOn(now)
                .updatedOn(now)
                .entryFee(BigDecimal.TEN)
                .timeControl(TEST_TIME_CONTROL)
                .paymentInstructions(TEST_PAYMENT_INSTRUCTIONS)
                .participationRequirements("NONE")
                .status(TEST_TOURNAMENT_STATUS)
                .build();
    }
}

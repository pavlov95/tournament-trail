package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TimeControl;
import tournament_trail.demo.web.dtos.TournamentRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TournamentRequestFixture {
    public static String TEST_NAME = "Tet name";
    public static String TEST_CITY = "Test city";
    public static String TEST_COUNTRY = "Test country";
    public static String TEST_VENUE = "Test venue";
    public static String TEST_DESCRIPTION = "Test description";
    public static String TEST_PAYMENT_INSTRUCTIONS = "Test payment instructions";
    public static CurrencyCode TEST_CURRENCY = CurrencyCode.EUR;
    public static TimeControl TEST_TIME_CONTROL = TimeControl.BULLET;
    public static Integer TEST_MAXIMUM_PARTICIPANTS = 100;
    public static Integer TEST_EDITION = 1;
    public static String TEST_PARTICIPATION_REQUIREMENTS = "TEST_REQUIREMENTS";
    public static BigDecimal TEST_ENTRY_FEE = BigDecimal.TEN;

    public static TournamentRequest create() {
        LocalDateTime now = LocalDateTime.now();
        TournamentRequest tournamentRequest = new TournamentRequest();
        tournamentRequest.setName(TEST_NAME);
        tournamentRequest.setCity(TEST_CITY);
        tournamentRequest.setCountry(TEST_COUNTRY);
        tournamentRequest.setVenue(TEST_VENUE);
        tournamentRequest.setRegistrationDeadline(now.plusDays(1));
        tournamentRequest.setStartTime(now.plusDays(2));
        tournamentRequest.setEndTime(now.plusDays(3));
        tournamentRequest.setCurrency(TEST_CURRENCY);
        tournamentRequest.setDescription(TEST_DESCRIPTION);
        tournamentRequest.setRated(true);
        tournamentRequest.setEntryFee(TEST_ENTRY_FEE);
        tournamentRequest.setTimeControl(TEST_TIME_CONTROL);
        tournamentRequest.setPaymentInstructions(TEST_PAYMENT_INSTRUCTIONS);
        tournamentRequest.setParticipationRequirements(TEST_PARTICIPATION_REQUIREMENTS);
        tournamentRequest.setEdition(TEST_EDITION);
        tournamentRequest.setMaximumParticipants(TEST_MAXIMUM_PARTICIPANTS);

        return tournamentRequest;
    }
}

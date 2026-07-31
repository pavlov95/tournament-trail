package tournament_trail.demo.fixtures;


import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.enums.TournamentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class TournamentFixture {
    public static Tournament createWithStatusStarted(UUID id, UUID organiserId) {
        return Tournament.builder()
                .status(TournamentStatus.STARTED)
                .id(id)
                .organiser(UserFixture.createUser(organiserId))
                .build();
    }

    public static Tournament createWithStatusRegistrationClosed() {
        return Tournament.builder()
                .status(TournamentStatus.REGISTRATION_CLOSED)
                .build();
    }

    public static Tournament createWithStatusCompleted() {
        return Tournament.builder()
                .status(TournamentStatus.COMPLETED)
                .build();
    }

    public static Tournament creteWithOrganiserIdAndStatusStarted(UUID tournamentId, UUID organiserID) {
        return Tournament.builder()
                .id(tournamentId)
                .status(TournamentStatus.STARTED)
                .organiser(UserFixture.createUser(organiserID))
                .build();
    }

    public static Tournament create() {
        return Tournament.builder()
                .id(UUID.randomUUID())
                .build();
    }

    public static Tournament createTournamentWithStatus(UUID id, TournamentStatus status) {
        return Tournament.builder()
                .id(id)
                .city("TestCity")
                .country("TestCountry")
                .venue("TestVenue")
                .status(status)
                .description("test")
                .startTime(LocalDateTime.now())
                .build();
    }

    public static Tournament createTournamentWithStartTime(UUID id, LocalDateTime localDateTime) {
        return Tournament.builder()
                .id(id)
                .city("TestCity")
                .country("TestCountry")
                .venue("TestVenue")
                .description("test")
                .startTime(localDateTime)
                .build();
    }


}

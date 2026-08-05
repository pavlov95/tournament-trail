package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.enums.PaymentStatus;
import tournament_trail.demo.entities.enums.RegistrationStatus;

import java.util.List;
import java.util.UUID;

public class TournamentRegistrationFixture {
    public static TournamentRegistration create() {
        return TournamentRegistration.builder()
                .player(UserFixture.createUser())
                .tournament(TournamentFixture.create())
                .registrationStatus(RegistrationStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }


    public static TournamentRegistration createWithStatusCancelled() {
        return TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(UserFixture.createUser())
                .tournament(TournamentFixture.create())
                .registrationStatus(RegistrationStatus.CANCELLED)
                .build();
    }

    public static TournamentRegistration createWithStatus(RegistrationStatus status) {
        return TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(UserFixture.createUser())
                .tournament(TournamentFixture.create())
                .registrationStatus(status)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    public static TournamentRegistration createWithRegistrationStatusPendingAndPaymentStatus(PaymentStatus status) {
        return TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(UserFixture.createUser())
                .tournament(TournamentFixture.create())
                .registrationStatus(RegistrationStatus.PENDING_PAYMENT)
                .paymentStatus(status)
                .build();
    }

    public static TournamentRegistration createWithRegistrationStatusPendingAndPaymentStatusPending() {
        return TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(UserFixture.createUser())
                .tournament(TournamentFixture.create())
                .registrationStatus(RegistrationStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    public static List<TournamentRegistration> createList() {
        TournamentRegistration first = createWithRegistrationStatusPendingAndPaymentStatusPending();
        TournamentRegistration second = createWithRegistrationStatusPendingAndPaymentStatusPending();

        return List.of(first, second);
    }
}
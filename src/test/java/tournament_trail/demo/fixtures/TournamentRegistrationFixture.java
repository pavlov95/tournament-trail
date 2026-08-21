package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.PaymentStatus;
import tournament_trail.demo.entities.enums.RegistrationStatus;

import java.time.LocalDateTime;
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

    public static TournamentRegistration createWithRegistrationStatusPendingAndPaymentStatusSubmitted() {
        return TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(UserFixture.createUser())
                .tournament(TournamentFixture.create())
                .registrationStatus(RegistrationStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    public static TournamentRegistration createWithRegistrationStatusPendingAndPaymentStatusSubmitted(
            User applicant, Tournament tournament) {

        return TournamentRegistration.builder()
                .player(applicant)
                .tournament(tournament)
                .registrationStatus(RegistrationStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.SUBMITTED)
                .paymentReference("Test")
                .organiserNote("Test")
                .hidden(false)
                .updatedOn(LocalDateTime.now())
                .registeredOn(LocalDateTime.now())
                .reservedUntil(LocalDateTime.now().plusHours(1))
                .paymentSubmittedOn(LocalDateTime.now())
                .build();
    }

    public static TournamentRegistration createWithRegistrationStatusCancelledAndPaymentStatusRejected(
            User applicant, Tournament tournament) {

        return TournamentRegistration.builder()
                .player(applicant)
                .tournament(tournament)
                .registrationStatus(RegistrationStatus.CANCELLED)
                .paymentStatus(PaymentStatus.REJECTED)
                .paymentReference("Test")
                .organiserNote("Test")
                .hidden(false)
                .updatedOn(LocalDateTime.now())
                .registeredOn(LocalDateTime.now())
                .reservedUntil(LocalDateTime.now().plusHours(1))
                .paymentSubmittedOn(LocalDateTime.now())
                .build();
    }

    public static TournamentRegistration createWithRegistrationStatusPendingAndPaymentStatusPending(
            User applicant, Tournament tournament) {

        return TournamentRegistration.builder()
                .player(applicant)
                .tournament(tournament)
                .registrationStatus(RegistrationStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentReference("Test")
                .organiserNote("Test")
                .hidden(false)
                .updatedOn(LocalDateTime.now())
                .registeredOn(LocalDateTime.now())
                .reservedUntil(LocalDateTime.now().plusHours(1))
                .paymentSubmittedOn(LocalDateTime.now())
                .build();
    }

    public static TournamentRegistration createWithRegistrationStatusPendingAndPaymentStatusConfirmed(
            User applicant, Tournament tournament) {

        return TournamentRegistration.builder()
                .player(applicant)
                .tournament(tournament)
                .registrationStatus(RegistrationStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.CONFIRMED)
                .paymentReference("Test")
                .organiserNote("Test")
                .hidden(false)
                .updatedOn(LocalDateTime.now())
                .registeredOn(LocalDateTime.now())
                .reservedUntil(LocalDateTime.now().plusHours(1))
                .paymentSubmittedOn(LocalDateTime.now())
                .build();
    }

    public static TournamentRegistration createWithRegistrationStatusRejectedAndPaymentStatusConfirmed(
            User applicant, Tournament tournament) {

        return TournamentRegistration.builder()
                .player(applicant)
                .tournament(tournament)
                .registrationStatus(RegistrationStatus.REJECTED)
                .paymentStatus(PaymentStatus.CONFIRMED)
                .paymentReference("Test")
                .organiserNote("Test")
                .hidden(false)
                .updatedOn(LocalDateTime.now())
                .registeredOn(LocalDateTime.now())
                .reservedUntil(LocalDateTime.now().plusHours(1))
                .paymentSubmittedOn(LocalDateTime.now())
                .build();
    }


    public static List<TournamentRegistration> createList() {
        TournamentRegistration first = createWithRegistrationStatusPendingAndPaymentStatusSubmitted();
        TournamentRegistration second = createWithRegistrationStatusPendingAndPaymentStatusSubmitted();

        return List.of(first, second);
    }
}
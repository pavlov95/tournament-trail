package tournament_trail.demo.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.enums.PaymentStatus;
import tournament_trail.demo.entities.enums.RegistrationStatus;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.exceptions.*;
import tournament_trail.demo.repositories.TournamentRegistrationRepository;
import tournament_trail.demo.web.dtos.PaymentRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TournamentRegistrationService {
    private final TournamentRegistrationRepository tournamentRegistrationRepository;
    private final TournamentService tournamentService;
    private final UserService userService;
    private static final String APPROVED_PAYMENT_MESSAGE =
            "Your payment has been confirmed. We look forward to seeing you at ";
    private static final String REJECTED_PAYMENT_MESSAGE = "Your registration has been declined by the organiser.";

    public TournamentRegistrationService(TournamentRegistrationRepository tournamentRegistrationRepository, TournamentService tournamentService, UserService userService) {
        this.tournamentRegistrationRepository = tournamentRegistrationRepository;
        this.tournamentService = tournamentService;
        this.userService = userService;
    }

    public List<TournamentRegistration> getAllRegistrationsByUserId(UUID userId) {
        return tournamentRegistrationRepository.findAllByPlayerId(userId);
    }

    public List<TournamentRegistration> getAllUnhiddenRegistrationsByUserId(UUID userId) {
        return tournamentRegistrationRepository
                .findAllByPlayerIdAndHiddenFalseOrderByRegisteredOnDesc(userId);
    }

    @Transactional
    public TournamentRegistration create(UUID userId, UUID tournamentId) {
        LocalDateTime now = LocalDateTime.now();

        Tournament tournament = tournamentService.findById(tournamentId);

        Optional<TournamentRegistration> existingRegistration =
                tournamentRegistrationRepository.findByTournamentIdAndPlayerId(tournamentId, userId);

        if (existingRegistration.isPresent()) {
            TournamentRegistration registration = existingRegistration.get();

            if (registration.getRegistrationStatus() == RegistrationStatus.CANCELLED) {
                tournamentService.validateTournamentConditions(tournament, now);
                tournamentService.validateTournamentNotFull(tournament.getMaximumParticipants()
                        ,countAllCurrentRegistrations(tournamentId));

                return reactivateCancelledRegistration(registration, tournament, now);
            }

            throw new AlreadyRegisteredException();
        }
        tournamentService.validateTournamentConditions(tournament, now);
        tournamentService.validateTournamentNotFull(tournament.getMaximumParticipants(),
                countAllCurrentRegistrations(tournamentId));

        TournamentRegistration.TournamentRegistrationBuilder builder = TournamentRegistration.builder()
                .tournament(tournament)
                .player(userService.findById(userId))
                .hidden(false)
                .registeredOn(now)
                .updatedOn(now);

        if (tournament.isFree()) {
            builder.paymentStatus(PaymentStatus.NOT_REQUIRED);
            builder.registrationStatus(RegistrationStatus.CONFIRMED);
        } else {
            builder.reservedUntil(now.plusHours(1));
            builder.paymentStatus(PaymentStatus.PENDING);
            builder.registrationStatus(RegistrationStatus.PENDING_PAYMENT);
        }

        return tournamentRegistrationRepository.save(builder.build());
    }

    public TournamentRegistration findById(UUID id) {
        return tournamentRegistrationRepository
                .findById(id).orElseThrow(InvalidTournamentRegistrationException::new);
    }


    @Transactional
    public void cancelRegistration(UUID registrationId, UUID userId, Role role) {
        TournamentRegistration registration = findById(registrationId);

        boolean isOwner = checkOwnership(registration, userId);
        boolean isAdmin = checkIfAdmin(role);
        boolean isOrganiser = checkIfOrganiser(registration, userId);

        if (!isOwner && !isAdmin && !isOrganiser) {
            throw new AccessDeniedException("You are not allowed to cancel this registration");
        }
        if (isTerminalStatus(registration.getRegistrationStatus())) {
            throw new IllegalStateException("This registration is already finished.");
        }

        if (isOwner) {
            registration.setRegistrationStatus(RegistrationStatus.CANCELLED);
        } else if (isOrganiser) {
            registration.setRegistrationStatus(RegistrationStatus.REJECTED);
        } else {
            registration.setRegistrationStatus(RegistrationStatus.CANCELLED_BY_ADMIN);
        }
        registration.setCancelledOn(LocalDateTime.now());
        registration.setUpdatedOn(LocalDateTime.now());
    }

    @Transactional
    public void hideRegistration(UUID registrationId, UUID userId) {
        TournamentRegistration registration = findById(registrationId);
        boolean isOwner = checkOwnership(registration, userId);

        boolean canHide = isTerminalStatus(registration.getRegistrationStatus());

        if (!isOwner) {
            throw new AccessDeniedException("You are not allowed to delete this registration");
        }
        if (!canHide) {
            throw new StatusNotTerminalException();
        }

        registration.setHidden(true);
        registration.setUpdatedOn(LocalDateTime.now());
    }

    public TournamentRegistration getRegistrationIfOwnerOrAdmin(UUID userId, UUID registrationId, Role role) {
        TournamentRegistration registration = findById(registrationId);
        boolean isOwner = checkOwnership(registration, userId);
        boolean isAdmin = checkIfAdmin(role);
        boolean isOrganiser = checkIfOrganiser(registration, userId);

        if (!isOwner && !isAdmin && !isOrganiser) {
            throw new AccessDeniedException("You are not allowed to view this registration");
        }
        return registration;
    }

    @Transactional
    public void addPayment(UUID id, PaymentRequest paymentRequest, UUID userId) {
        TournamentRegistration registration = findById(id);
        boolean isOwner = checkOwnership(registration, userId);
        if (!isOwner) {
            throw new AccessDeniedException("You are not allowed to make a payment on behalf of another player");
        }
        if (registration.getRegistrationStatus() != RegistrationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Payment can only be submitted for pending payment registrations.");
        }
        if (registration.getPaymentStatus() != PaymentStatus.PENDING &&
                registration.getPaymentStatus() != PaymentStatus.REJECTED) {
            throw new IllegalStateException("Payment reference cannot be submitted for this payment status.");
        }
        if (registration.getReservedUntil() == null
                || !registration.getReservedUntil().isAfter(LocalDateTime.now())) {
            registration.setRegistrationStatus(RegistrationStatus.EXPIRED);
            registration.setPaymentStatus(PaymentStatus.EXPIRED);
            registration.setUpdatedOn(LocalDateTime.now());

            throw new RegistrationReservationExpiredException();
        }


        registration.setPaymentStatus(PaymentStatus.SUBMITTED);
        registration.setPaymentReference(paymentRequest.getPaymentReference().trim());
        registration.setPaymentSubmittedOn(LocalDateTime.now());
        registration.setUpdatedOn(LocalDateTime.now());

        tournamentRegistrationRepository.save(registration);
    }

    @Transactional
    public void expirePendingTournamentRegistrations() {
        LocalDateTime now = LocalDateTime.now();
        List<TournamentRegistration> expiredRegistrations = tournamentRegistrationRepository
                .findAllByRegistrationStatusAndPaymentStatusAndReservedUntilBefore
                        (RegistrationStatus.PENDING_PAYMENT, PaymentStatus.PENDING, now);

        for (TournamentRegistration registration : expiredRegistrations) {
            registration.setRegistrationStatus(RegistrationStatus.EXPIRED);
            registration.setPaymentStatus(PaymentStatus.EXPIRED);
            registration.setUpdatedOn(now);
        }
    }

    public List<TournamentRegistration> getRegistrationsForTournamentManagement(UUID tournamentId, UUID userId, Role role) {
        Tournament tournament = tournamentService.findById(tournamentId);
        boolean isAdmin = checkIfAdmin(role);
        boolean isOrganiser = tournament.getOrganiser().getId().equals(userId);
        if (!isAdmin && !isOrganiser) {
            throw new AccessDeniedException("You are not allowed to view this information");
        }
        return tournamentRegistrationRepository.findAllByTournamentIdOrderByRegisteredOnDesc(tournament.getId());
    }

    @Transactional
    public void approvePayment(UUID registrationId, UUID userId, Role role) {
        TournamentRegistration registration = verifyRoleForPayment(registrationId, userId, role);

        registration.setRegistrationStatus(RegistrationStatus.CONFIRMED);
        registration.setPaymentStatus(PaymentStatus.CONFIRMED);
        registration.setUpdatedOn(LocalDateTime.now());
        registration.setOrganiserNote(APPROVED_PAYMENT_MESSAGE + registration.getTournament().getName() + ".");
    }

    @Transactional
    public void rejectPayment(UUID registrationId, UUID userId, Role role, String organiserNote) {
        TournamentRegistration registration = verifyRoleForPayment(registrationId, userId, role);

        registration.setPaymentStatus(PaymentStatus.REJECTED);
        registration.setRegistrationStatus(RegistrationStatus.REJECTED);
        registration.setUpdatedOn(LocalDateTime.now());
        if (organiserNote == null || organiserNote.isBlank()) {
            registration.setOrganiserNote(REJECTED_PAYMENT_MESSAGE);
        } else {
            registration.setOrganiserNote(organiserNote.trim());
        }
    }

    private TournamentRegistration verifyRoleForPayment(UUID registrationId, UUID userId, Role role) {
        TournamentRegistration registration = findById(registrationId);
        boolean isAdmin = checkIfAdmin(role);
        boolean isOrganiser = registration.getTournament().getOrganiser().getId().equals(userId);
        if (!isAdmin && !isOrganiser) {
            throw new AccessDeniedException("You are not allowed to review this payment.");
        }
        if (registration.getPaymentStatus() != PaymentStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted payments can be reviewed.");
        }
        return registration;
    }

    private boolean checkOwnership(TournamentRegistration registration, UUID userId) {
        return registration.getPlayer().getId().equals(userId);
    }

    private boolean checkIfAdmin(Role role) {
        return role == Role.ADMIN;
    }

    private boolean checkIfOrganiser(TournamentRegistration registration, UUID userId) {
        return registration.getTournament().getOrganiser().getId().equals(userId);
    }

    private boolean isTerminalStatus(RegistrationStatus status) {
        return status != RegistrationStatus.PENDING_PAYMENT
                && status != RegistrationStatus.CONFIRMED;
    }


    private TournamentRegistration reactivateCancelledRegistration(TournamentRegistration registration
            , Tournament tournament, LocalDateTime now) {

        registration.setHidden(false);
        registration.setCancelledOn(null);
        registration.setPaymentReference(null);
        registration.setOrganiserNote(null);
        registration.setPaymentSubmittedOn(null);
        registration.setRegisteredOn(now);
        registration.setUpdatedOn(now);

        if (tournament.isFree()) {
            registration.setPaymentStatus(PaymentStatus.NOT_REQUIRED);
            registration.setRegistrationStatus(RegistrationStatus.CONFIRMED);
            registration.setReservedUntil(null);
        } else {
            registration.setPaymentStatus(PaymentStatus.PENDING);
            registration.setRegistrationStatus(RegistrationStatus.PENDING_PAYMENT);
            registration.setReservedUntil(now.plusHours(1));
        }

        return registration;
    }

    private int countAllCurrentRegistrations(UUID tournamentId) {
        return  tournamentRegistrationRepository
                .countByTournamentIdAndRegistrationStatusIn(tournamentId,
                        List.of(RegistrationStatus.PENDING_PAYMENT, RegistrationStatus.CONFIRMED));
    }

}

package tournament_trail.demo.services;

import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.enums.PaymentStatus;
import tournament_trail.demo.entities.enums.RegistrationStatus;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.enums.TournamentStatus;
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
                validateTournamentConditions(tournament, now);

                return reactivateCancelledRegistration(registration, tournament, now);
            }

            throw new AlreadyRegisteredException();
        }

        validateTournamentConditions(tournament, now);
        validateTournamentNotFull(tournament);

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

        validateTournamentConditions(registration.getTournament(), LocalDateTime.now());

        if (!registration.getReservedUntil().isAfter(LocalDateTime.now()) ||
                registration.getRegistrationStatus() == RegistrationStatus.EXPIRED) {
            throw new RegistrationReservationExpiredException();
        }

        registration.setPaymentStatus(PaymentStatus.SUBMITTED);
        registration.setPaymentReference(paymentRequest.getPaymentReference());
        registration.setPaymentSubmittedOn(LocalDateTime.now());
        registration.setUpdatedOn(LocalDateTime.now());

        tournamentRegistrationRepository.save(registration);
    }

    @Transactional
    public void expirePendingTournamentRegistrations() {
        LocalDateTime now = LocalDateTime.now();
        List<TournamentRegistration> expiredRegistrations = tournamentRegistrationRepository
                .findAllByRegistrationStatusAndReservedUntilBefore(RegistrationStatus.PENDING_PAYMENT, now);

        for (TournamentRegistration registration : expiredRegistrations) {
            registration.setRegistrationStatus(RegistrationStatus.EXPIRED);
            registration.setPaymentStatus(PaymentStatus.EXPIRED);
            registration.setUpdatedOn(now);
        }
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

    private void validateTournamentConditions(Tournament tournament, LocalDateTime now) {
        TournamentStatus status = tournament.getStatus();
        LocalDateTime startTime = tournament.getStartTime();
        LocalDateTime registrationDeadline = tournament.getRegistrationDeadline();

        if (status == TournamentStatus.CANCELLED) {
            throw new TournamentCancelledException();
        }
        if (status == TournamentStatus.REGISTRATION_CLOSED) {
            throw new AccessDeniedException("Registration is closed for this tournament.");
        }
        if (status != TournamentStatus.PUBLISHED) {
            throw new AccessDeniedException("You are only allowed to register for Published tournaments");
        }
        if (!startTime.isAfter(now)) {
            throw new TournamentHasAlreadyStartedException();
        }
        if (!registrationDeadline.isAfter(now)) {
            throw new AccessDeniedException("Registration has ended");
        }

    }

    private void validateTournamentNotFull(Tournament tournament) {
        int maximumParticipants = tournament.getMaximumParticipants();
        int currentRegistrations = tournamentRegistrationRepository
                .countByTournamentIdAndRegistrationStatusIn(tournament.getId(),
                        List.of(RegistrationStatus.PENDING_PAYMENT, RegistrationStatus.CONFIRMED));

        if (maximumParticipants <= currentRegistrations) {
            throw new TournamentFullException();
        }
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


}

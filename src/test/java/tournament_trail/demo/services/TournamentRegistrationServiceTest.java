package tournament_trail.demo.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;

import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.PaymentStatus;
import tournament_trail.demo.entities.enums.RegistrationStatus;

import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.exceptions.*;
import tournament_trail.demo.fixtures.PaymentRequestFixture;
import tournament_trail.demo.fixtures.TournamentFixture;
import tournament_trail.demo.fixtures.TournamentRegistrationFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.TournamentRegistrationRepository;
import tournament_trail.demo.web.dtos.PaymentRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TournamentRegistrationServiceTest {
    @Mock
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    @Mock
    private TournamentService tournamentService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TournamentRegistrationService tournamentRegistrationService;

    @Captor
    private ArgumentCaptor<TournamentRegistration> captor;

    private static final String APPROVED_PAYMENT_MESSAGE =
            "Your payment has been confirmed. We look forward to seeing you at ";
    private static final String REJECTED_PAYMENT_MESSAGE = "Your registration has been declined by the organiser.";

    private static Stream<Arguments> expiredReservationTimes() {
        return Stream.of(
                Arguments.of((LocalDateTime) null),
                Arguments.of(LocalDateTime.now().minusDays(1))
        );
    }

    private static Stream<Arguments> rejectPaymentNoteCases() {
        return Stream.of(
                Arguments.of(null, REJECTED_PAYMENT_MESSAGE),
                Arguments.of("", REJECTED_PAYMENT_MESSAGE),
                Arguments.of("   ", REJECTED_PAYMENT_MESSAGE),
                Arguments.of("TEST", "TEST"),
                Arguments.of("  TEST  ", "TEST")
        );
    }

    @Test
    public void create_shouldThrowTournamentDoesNotExistException_whenInvalidTournament() {
        UUID tournamentId = UUID.randomUUID();

        when(tournamentService.findById(tournamentId)).thenThrow(TournamentDoesNotExist.class);
        assertThrows(TournamentDoesNotExist.class
                , () -> tournamentRegistrationService.create(UUID.randomUUID(), tournamentId));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));
    }

    @ParameterizedTest
    @EnumSource(value = RegistrationStatus.class,
            mode = EnumSource.Mode.EXCLUDE,
            names = "CANCELLED")
    public void create_shouldThrowAlreadyRegisteredException_whenRegistrationStatusNotCancelled(
            RegistrationStatus status) {

        Tournament tournament = TournamentFixture.create();
        UUID userId = UUID.randomUUID();

        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();
        tournamentRegistration.setRegistrationStatus(status);

        when(tournamentService.findById(tournament.getId()))
                .thenReturn(tournament);

        when(tournamentRegistrationRepository.findByTournamentIdAndPlayerId(
                tournament.getId(), userId))
                .thenReturn(Optional.of(tournamentRegistration));

        assertThrows(AlreadyRegisteredException.class,
                () -> tournamentRegistrationService.create(userId, tournament.getId()));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));
    }

    @Test
    public void create_shouldThrowIllegalStateException_whenTournamentStatusIsCancelled() {
        Tournament tournament = TournamentFixture.createWithStatusCancelled();
        UUID userId = UUID.randomUUID();
        TournamentRegistration tournamentRegistration =
                TournamentRegistrationFixture.createWithStatusCancelled();

        when(tournamentService.findById(tournament.getId()))
                .thenReturn(tournament);

        when(tournamentRegistrationRepository.findByTournamentIdAndPlayerId(
                tournament.getId(), userId))
                .thenReturn(Optional.of(tournamentRegistration));

        doThrow(new IllegalStateException("This tournament has been cancelled"))
                .when(tournamentService)
                .validateTournamentConditions(eq(tournament), any(LocalDateTime.class));

        assertThrows(IllegalStateException.class
                , () -> tournamentRegistrationService.create(userId, tournament.getId()));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, names = {"DRAFT", "STARTED", "COMPLETED"})
    public void create_shouldThrowAccessDeniedException_whenTournamentStatusNotValidForRegistration(
            TournamentStatus status) {
        Tournament tournament = TournamentFixture.createWithStatus(status);
        UUID userId = UUID.randomUUID();
        TournamentRegistration tournamentRegistration =
                TournamentRegistrationFixture.createWithStatusCancelled();

        when(tournamentService.findById(tournament.getId()))
                .thenReturn(tournament);

        when(tournamentRegistrationRepository.findByTournamentIdAndPlayerId(
                tournament.getId(), userId))
                .thenReturn(Optional.of(tournamentRegistration));

        doThrow(new AccessDeniedException("You are only allowed to register for Published tournaments"))
                .when(tournamentService)
                .validateTournamentConditions(eq(tournament), any(LocalDateTime.class));

        assertThrows(AccessDeniedException.class
                , () -> tournamentRegistrationService.create(userId, tournament.getId()));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));
    }


    @Test
    public void create_shouldThrowTournamentHasAlreadyStartedException_whenTournamentHasStarted() {
        Tournament tournament = TournamentFixture.createWithStatusPublished();
        tournament.setStartTime(LocalDateTime.now().minusMinutes(20));
        UUID userId = UUID.randomUUID();
        TournamentRegistration tournamentRegistration =
                TournamentRegistrationFixture.createWithStatusCancelled();

        when(tournamentService.findById(tournament.getId()))
                .thenReturn(tournament);

        when(tournamentRegistrationRepository.findByTournamentIdAndPlayerId(
                tournament.getId(), userId))
                .thenReturn(Optional.of(tournamentRegistration));

        doThrow(new TournamentHasAlreadyStartedException())
                .when(tournamentService)
                .validateTournamentConditions(eq(tournament), any(LocalDateTime.class));

        assertThrows(TournamentHasAlreadyStartedException.class
                , () -> tournamentRegistrationService.create(userId, tournament.getId()));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));
    }

    @Test
    public void create_shouldCreateValidTournamentRegistration_whenTournamentIsFree() {
        Tournament tournament = TournamentFixture.createWithStatusPublished();
        tournament.setEntryFee(BigDecimal.ZERO);

        User user = UserFixture.createUser();

        when(tournamentService.findById(tournament.getId()))
                .thenReturn(tournament);

        when(tournamentRegistrationRepository.findByTournamentIdAndPlayerId(
                tournament.getId(), user.getId())).thenReturn(Optional.empty());

        when(userService.findById(user.getId())).thenReturn(user);

        tournamentRegistrationService.create(user.getId(), tournament.getId());

        verify(tournamentService).validateTournamentConditions(
                eq(tournament), any(LocalDateTime.class));

        verify(tournamentService).validateTournamentNotFull(
                eq(tournament.getMaximumParticipants()), anyInt());

        verify(tournamentRegistrationRepository).save(captor.capture());

        TournamentRegistration result = captor.getValue();

        assertEquals(tournament, result.getTournament());
        assertEquals(user, result.getPlayer());
        assertFalse(result.isHidden());
        assertNotNull(result.getRegisteredOn());
        assertNotNull(result.getUpdatedOn());
        assertNull(result.getReservedUntil());
        assertEquals(PaymentStatus.NOT_REQUIRED, result.getPaymentStatus());
        assertEquals(RegistrationStatus.CONFIRMED, result.getRegistrationStatus());
    }

    @Test
    public void create_shouldCreateValidTournamentRegistration_whenTournamentIsNotFree() {
        Tournament tournament = TournamentFixture.createWithStatusPublished();
        tournament.setEntryFee(BigDecimal.TEN);

        User user = UserFixture.createUser();

        when(tournamentService.findById(tournament.getId()))
                .thenReturn(tournament);

        when(tournamentRegistrationRepository.findByTournamentIdAndPlayerId(
                tournament.getId(), user.getId()))
                .thenReturn(Optional.empty());

        when(userService.findById(user.getId()))
                .thenReturn(user);

        LocalDateTime before = LocalDateTime.now();

        tournamentRegistrationService.create(user.getId(), tournament.getId());

        verify(tournamentService).validateTournamentConditions(
                eq(tournament), any(LocalDateTime.class));

        verify(tournamentService).validateTournamentNotFull(
                eq(tournament.getMaximumParticipants()), anyInt());

        verify(tournamentRegistrationRepository).save(captor.capture());

        TournamentRegistration result = captor.getValue();

        assertEquals(tournament, result.getTournament());
        assertEquals(user, result.getPlayer());
        assertFalse(result.isHidden());
        assertNotNull(result.getRegisteredOn());
        assertNotNull(result.getUpdatedOn());
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        assertEquals(RegistrationStatus.PENDING_PAYMENT, result.getRegistrationStatus());
        assertNotNull(result.getReservedUntil());
        assertFalse(result.getReservedUntil().isBefore(before));
    }

    @Test
    public void create_shouldReactivateRegistration_whenTournamentIsNotFree() {
        Tournament tournament = TournamentFixture.createPaidWithStatusPublished();

        User user = UserFixture.createUser();

        TournamentRegistration tournamentRegistration =
                TournamentRegistrationFixture.createWithStatusCancelled();

        tournamentRegistration.setTournament(tournament);
        tournamentRegistration.setPlayer(user);

        when(tournamentService.findById(tournament.getId()))
                .thenReturn(tournament);

        when(tournamentRegistrationRepository.findByTournamentIdAndPlayerId(
                tournament.getId(), user.getId()))
                .thenReturn(Optional.of(tournamentRegistration));

        when(tournamentRegistrationRepository.countByTournamentIdAndRegistrationStatusIn(
                eq(tournament.getId()), anyList())).thenReturn(0);

        LocalDateTime before = LocalDateTime.now();

        TournamentRegistration result = tournamentRegistrationService.create(
                user.getId(), tournament.getId());

        verify(tournamentService).validateTournamentConditions(
                eq(tournament), any(LocalDateTime.class));

        verify(tournamentService).validateTournamentNotFull(
                eq(tournament.getMaximumParticipants()), eq(0));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));

        assertSame(tournamentRegistration, result);
        assertEquals(tournament, result.getTournament());
        assertEquals(user, result.getPlayer());

        assertFalse(result.isHidden());
        assertNull(result.getOrganiserNote());
        assertNull(result.getCancelledOn());
        assertNull(result.getPaymentReference());
        assertNull(result.getPaymentSubmittedOn());

        assertFalse(result.getRegisteredOn().isBefore(before));
        assertFalse(result.getUpdatedOn().isBefore(before));
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        assertEquals(RegistrationStatus.PENDING_PAYMENT, result.getRegistrationStatus());
        assertNotNull(result.getReservedUntil());
        assertFalse(result.getReservedUntil().isBefore(before));
    }

    @Test
    public void create_shouldReactivateRegistration_whenTournamentIsFree() {
        Tournament tournament = TournamentFixture.createWithStatusPublished();

        User user = UserFixture.createUser();

        TournamentRegistration tournamentRegistration =
                TournamentRegistrationFixture.createWithStatusCancelled();

        tournamentRegistration.setTournament(tournament);
        tournamentRegistration.setPlayer(user);

        when(tournamentService.findById(tournament.getId()))
                .thenReturn(tournament);

        when(tournamentRegistrationRepository.findByTournamentIdAndPlayerId(
                tournament.getId(), user.getId()))
                .thenReturn(Optional.of(tournamentRegistration));

        when(tournamentRegistrationRepository.countByTournamentIdAndRegistrationStatusIn(
                eq(tournament.getId()), anyList()))
                .thenReturn(0);

        LocalDateTime before = LocalDateTime.now();

        TournamentRegistration result = tournamentRegistrationService.create(
                user.getId(), tournament.getId());

        verify(tournamentService).validateTournamentConditions(
                eq(tournament), any(LocalDateTime.class));

        verify(tournamentService).validateTournamentNotFull(
                eq(tournament.getMaximumParticipants()), eq(0));

        verify(tournamentRegistrationRepository, never())
                .save(any(TournamentRegistration.class));

        assertSame(tournamentRegistration, result);
        assertEquals(tournament, result.getTournament());
        assertEquals(user, result.getPlayer());

        assertFalse(result.isHidden());
        assertNull(result.getOrganiserNote());
        assertNull(result.getCancelledOn());
        assertNull(result.getPaymentReference());
        assertNull(result.getPaymentSubmittedOn());

        assertFalse(result.getRegisteredOn().isBefore(before));
        assertFalse(result.getUpdatedOn().isBefore(before));

        assertEquals(PaymentStatus.NOT_REQUIRED, result.getPaymentStatus());
        assertEquals(RegistrationStatus.CONFIRMED, result.getRegistrationStatus());
        assertNull(result.getReservedUntil());
    }

    @Test
    public void findById_shouldThrowInvalidTournamentRegistrationException_whenInvalidRegistration() {
        UUID tournamentRegistrationId = UUID.randomUUID();
        when(tournamentRegistrationRepository.findById(tournamentRegistrationId))
                .thenReturn(Optional.empty());
        assertThrows(InvalidTournamentRegistrationException.class
                , () -> tournamentRegistrationService.findById(tournamentRegistrationId));
    }

    @Test
    public void findById_shouldReturnTournamentRegistration_whenRegistrationIsValid() {
        TournamentRegistration result = TournamentRegistrationFixture.create();
        when(tournamentRegistrationRepository.findById(result.getId()))
                .thenReturn(Optional.of(result));
        assertEquals(result, tournamentRegistrationService.findById(result.getId()));
    }

    @Test
    public void cancelRegistration_shouldThrowAccessDeniedException_whenUserIsNotOwnerNorOrganiserNorAdmin() {
        TournamentRegistration registration = TournamentRegistrationFixture.create();

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(AccessDeniedException.class
                , () -> tournamentRegistrationService.cancelRegistration(
                        registration.getId(), UUID.randomUUID(), Role.PLAYER));
    }

    @ParameterizedTest
    @EnumSource(value = RegistrationStatus.class
            , names = {"EXPIRED", "REJECTED", "CANCELLED", "CANCELLED_BY_ADMIN"})
    public void cancelRegistration_shouldThrowIllegalStateException_whenStatusIsTerminal(
            RegistrationStatus status) {

        TournamentRegistration registration = TournamentRegistrationFixture.create();
        registration.setRegistrationStatus(status);

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(IllegalStateException.class
                , () -> tournamentRegistrationService.cancelRegistration(
                        registration.getId(), registration.getPlayer().getId(), Role.PLAYER));
    }

    @ParameterizedTest
    @EnumSource(value = RegistrationStatus.class, names = {"CONFIRMED", "PENDING_PAYMENT"})
    public void cancelRegistration_shouldCancel_whenUserIsOwner(RegistrationStatus status) {

        TournamentRegistration registration =
                TournamentRegistrationFixture.createWithStatus(status);

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        tournamentRegistrationService.cancelRegistration(
                registration.getId(), registration.getPlayer().getId(), Role.PLAYER);

        assertEquals(RegistrationStatus.CANCELLED, registration.getRegistrationStatus());
        assertNotNull(registration.getCancelledOn());
        assertNotNull(registration.getUpdatedOn());
    }

    @ParameterizedTest
    @EnumSource(value = RegistrationStatus.class, names = {"CONFIRMED", "PENDING_PAYMENT"})
    public void cancelRegistration_shouldReject_whenUserIsOrganiser(RegistrationStatus status) {

        TournamentRegistration registration =
                TournamentRegistrationFixture.createWithStatus(status);

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        tournamentRegistrationService.cancelRegistration(registration.getId()
                , registration.getTournament().getOrganiser().getId(), Role.PLAYER);

        assertEquals(RegistrationStatus.REJECTED, registration.getRegistrationStatus());
        assertNotNull(registration.getCancelledOn());
        assertNotNull(registration.getUpdatedOn());
    }

    @ParameterizedTest
    @EnumSource(value = RegistrationStatus.class, names = {"CONFIRMED", "PENDING_PAYMENT"})
    public void cancelRegistration_shouldCancelByAdmin_whenUserIsAdmin
            (RegistrationStatus status) {

        TournamentRegistration registration =
                TournamentRegistrationFixture.createWithStatus(status);

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        tournamentRegistrationService.cancelRegistration(
                registration.getId(), UUID.randomUUID(), Role.ADMIN);

        assertEquals(RegistrationStatus.CANCELLED_BY_ADMIN, registration.getRegistrationStatus());
        assertNotNull(registration.getCancelledOn());
        assertNotNull(registration.getUpdatedOn());
    }

    @Test
    public void hideRegistration_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        TournamentRegistration registration = TournamentRegistrationFixture.create();

        when(tournamentRegistrationRepository
                .findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(AccessDeniedException.class
                , () -> tournamentRegistrationService.hideRegistration(
                        registration.getId(), UUID.randomUUID()));
    }

    @ParameterizedTest
    @EnumSource(value = RegistrationStatus.class
            , names = {"CONFIRMED", "PENDING_PAYMENT"})
    public void hideRegistration_shouldThrowIllegalStateException_whenStatusIsNotTerminal(
            RegistrationStatus status) {

        TournamentRegistration registration =
                TournamentRegistrationFixture.createWithStatus(status);

        when(tournamentRegistrationRepository
                .findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(IllegalStateException.class
                , () -> tournamentRegistrationService.hideRegistration(
                        registration.getId(), registration.getPlayer().getId()));
    }

    @ParameterizedTest
    @EnumSource(value = RegistrationStatus.class
            , names = {"REJECTED", "EXPIRED", "CANCELLED_BY_ADMIN", "CANCELLED"})
    public void hideRegistration_shouldHideRegistration_whenStatusTerminal(
            RegistrationStatus status) {

        TournamentRegistration registration =
                TournamentRegistrationFixture.createWithStatus(status);

        when(tournamentRegistrationRepository
                .findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        LocalDateTime before = LocalDateTime.now();
        tournamentRegistrationService.hideRegistration(
                registration.getId(), registration.getPlayer().getId());

        assertTrue(registration.isHidden());
        assertFalse(registration.getUpdatedOn().isBefore(before));
    }

    @Test
    public void getRegistrationIfOwnerOrAdmin_shouldThrowAccessDeniedException_whenNotOwnerNorAdminNorOrganiser() {

        TournamentRegistration registration = TournamentRegistrationFixture.create();

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(AccessDeniedException.class
                , () -> tournamentRegistrationService.getRegistrationIfOwnerOrAdmin(
                        UUID.randomUUID(), registration.getId(), Role.PLAYER));
    }

    @Test
    public void getRegistrationIfOwnerOrAdmin_shouldReturnRegistration_whenUserIsOwner() {

        TournamentRegistration registration = TournamentRegistrationFixture.create();
        UUID ownerId = registration.getPlayer().getId();

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertEquals(registration, tournamentRegistrationService.getRegistrationIfOwnerOrAdmin(
                ownerId, registration.getId(), Role.PLAYER));
    }

    @Test
    public void addPayment_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        TournamentRegistration registration = TournamentRegistrationFixture.create();

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(AccessDeniedException.class
                , () -> tournamentRegistrationService.addPayment(
                        registration.getId(), new PaymentRequest(), UUID.randomUUID()));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));
    }

    @ParameterizedTest
    @EnumSource(value = RegistrationStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "PENDING_PAYMENT")
    public void addPayment_shouldThrowIllegalStateException_whenRegistrationIsNotPendingPayment(
            RegistrationStatus status) {
        TournamentRegistration registration = TournamentRegistrationFixture.createWithStatus(status);

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(IllegalStateException.class
                , () -> tournamentRegistrationService.addPayment(
                        registration.getId(), new PaymentRequest(), registration.getPlayer().getId()));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"REJECTED", "PENDING"})
    public void addPayment_shouldThrowIllegalStateException_whenPaymentStatusIsNotPendingNorRejected(
            PaymentStatus status) {
        TournamentRegistration registration =
                TournamentRegistrationFixture.createWithRegistrationStatusPendingAndPaymentStatus(status);

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(IllegalStateException.class
                , () -> tournamentRegistrationService.addPayment(
                        registration.getId(), new PaymentRequest(), registration.getPlayer().getId()));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));
    }

    @ParameterizedTest
    @MethodSource("expiredReservationTimes")
    public void addPayment_shouldThrowRegistrationReservationExpiredException_whenReservationHasExpiredOrNull(
            LocalDateTime reservedUntil) {
        TournamentRegistration registration =
                TournamentRegistrationFixture.createWithRegistrationStatusPendingAndPaymentStatusPending();
        registration.setReservedUntil(reservedUntil);

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        LocalDateTime before = LocalDateTime.now();
        assertThrows(RegistrationReservationExpiredException.class
                , () -> tournamentRegistrationService.addPayment(
                        registration.getId(), new PaymentRequest(), registration.getPlayer().getId()));
        assertEquals(RegistrationStatus.EXPIRED, registration.getRegistrationStatus());
        assertEquals(PaymentStatus.EXPIRED, registration.getPaymentStatus());
        assertFalse(registration.getUpdatedOn().isBefore(before));

        verify(tournamentRegistrationRepository, never()).save(any(TournamentRegistration.class));
    }

    @Test
    public void addPayment_shouldAddPaymentSuccessfully() {
        PaymentRequest paymentRequest = PaymentRequestFixture.create();

        TournamentRegistration registration =
                TournamentRegistrationFixture.createWithRegistrationStatusPendingAndPaymentStatusPending();
        registration.setReservedUntil(LocalDateTime.now().plusMinutes(1));

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        LocalDateTime before = LocalDateTime.now();
        tournamentRegistrationService.addPayment(
                registration.getId(), paymentRequest, registration.getPlayer().getId());

        verify(tournamentRegistrationRepository).save(captor.capture());
        TournamentRegistration result = captor.getValue();

        assertEquals(PaymentStatus.SUBMITTED, result.getPaymentStatus());
        assertFalse(result.getPaymentSubmittedOn().isBefore(before));
        assertFalse(result.getUpdatedOn().isBefore(before));
        assertEquals("TEST", result.getPaymentReference());
    }

    @Test
    public void expirePendingTournamentRegistrations_shouldExpireRegistrationsSuccessfully() {
        List<TournamentRegistration> registrations = TournamentRegistrationFixture.createList();

        when(tournamentRegistrationRepository
                .findAllByRegistrationStatusAndPaymentStatusAndReservedUntilBefore(
                        eq(RegistrationStatus.PENDING_PAYMENT)
                        , eq(PaymentStatus.PENDING)
                        , any(LocalDateTime.class)))
                .thenReturn(registrations);

        LocalDateTime before = LocalDateTime.now();
        tournamentRegistrationService.expirePendingTournamentRegistrations();

        registrations.forEach(x -> {
            assertEquals(PaymentStatus.EXPIRED, x.getPaymentStatus());
            assertEquals(RegistrationStatus.EXPIRED, x.getRegistrationStatus());
            assertFalse(x.getUpdatedOn().isBefore(before));
        });

        verify(tournamentRegistrationRepository)
                .findAllByRegistrationStatusAndPaymentStatusAndReservedUntilBefore(
                        eq(RegistrationStatus.PENDING_PAYMENT),
                        eq(PaymentStatus.PENDING),
                        any(LocalDateTime.class));
    }

    @Test
    public void getRegistrationsForTournamentManagement_shouldThrowAccessDeniedException_whenUserIsNotAdminNorOrganiser() {
        Tournament tournament = TournamentFixture.create();

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        assertThrows(AccessDeniedException.class
                , () -> tournamentRegistrationService.getRegistrationsForTournamentManagement(
                        tournament.getId(), UUID.randomUUID(), Role.PLAYER));
    }

    @Test
    public void getRegistrationsForTournamentManagement_shouldReturnList_whenUserIsAdmin() {
        Tournament tournament = TournamentFixture.create();
        List<TournamentRegistration> expected = TournamentRegistrationFixture.createList();

        when(tournamentService.findById(tournament.getId()))
                .thenReturn(tournament);

        when(tournamentRegistrationRepository.findAllByTournamentIdOrderByRegisteredOnDesc(
                tournament.getId())).thenReturn(expected);

        List<TournamentRegistration> result = tournamentRegistrationService
                .getRegistrationsForTournamentManagement(tournament.getId(), UUID.randomUUID(), Role.ADMIN);

        assertEquals(expected, result);
    }

    @Test
    public void getRegistrationsForTournamentManagement_shouldReturnListOfTournamentRegistrations() {
        Tournament tournament = TournamentFixture.create();
        List<TournamentRegistration> expected = TournamentRegistrationFixture.createList();

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        when(tournamentRegistrationRepository.findAllByTournamentIdOrderByRegisteredOnDesc(
                tournament.getId()))
                .thenReturn(expected);

        List<TournamentRegistration> result = tournamentRegistrationService.getRegistrationsForTournamentManagement(
                tournament.getId(), tournament.getOrganiser().getId(), Role.PLAYER);

        assertEquals(expected, result);
    }

    @Test
    public void approvePayment_shouldThrowAccessDeniedException_whenUserIsNotAdminNorOrganiser() {
        TournamentRegistration registration = TournamentRegistrationFixture.create();

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(AccessDeniedException.class
                , () -> tournamentRegistrationService.approvePayment(
                        registration.getId(), UUID.randomUUID(), Role.PLAYER));

    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "SUBMITTED")
    public void approvePayment_shouldThrowIllegalStateException_whenPaymentStatusIsNotSubmitted(
            PaymentStatus status) {

        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatus(status);

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        assertThrows(IllegalStateException.class
                , () -> tournamentRegistrationService.approvePayment(
                        registration.getId(), registration.getTournament().getOrganiser().getId(), Role.PLAYER));

    }

    @Test
    public void approvePayment_shouldApprovePaymentSuccessfully() {

        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatus(PaymentStatus.SUBMITTED);

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        LocalDateTime before = LocalDateTime.now();
        tournamentRegistrationService.approvePayment(
                registration.getId(), registration.getTournament().getOrganiser().getId(), Role.PLAYER);

        assertEquals(RegistrationStatus.CONFIRMED, registration.getRegistrationStatus());
        assertEquals(PaymentStatus.CONFIRMED, registration.getPaymentStatus());
        assertFalse(registration.getUpdatedOn().isBefore(before));

        String expected = APPROVED_PAYMENT_MESSAGE + registration.getTournament().getName() + ".";

        assertEquals(expected, registration.getOrganiserNote());
    }

    @ParameterizedTest
    @MethodSource("rejectPaymentNoteCases")
    public void rejectPayment_shouldRejectPaymentSuccessfully(String organiserNote
            , String expectedOrganiserNote) {

        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatus(PaymentStatus.SUBMITTED);
        UUID organiserId = registration.getTournament().getOrganiser().getId();

        when(tournamentRegistrationRepository.findById(registration.getId()))
                .thenReturn(Optional.of(registration));

        LocalDateTime before = LocalDateTime.now();
        tournamentRegistrationService.rejectPayment(registration.getId(), organiserId, Role.PLAYER, organiserNote);

        assertEquals(PaymentStatus.REJECTED, registration.getPaymentStatus());
        assertEquals(RegistrationStatus.REJECTED, registration.getRegistrationStatus());
        assertFalse(registration.getUpdatedOn().isBefore(before));
        assertEquals(expectedOrganiserNote, registration.getOrganiserNote());
    }
    @Test
    public void getAllRegistrationsByUser_shouldReturnListOfTournamentRegistrations(){
        UUID userId = UUID.randomUUID();
        List<TournamentRegistration> result = TournamentRegistrationFixture.createList();

        when(tournamentRegistrationRepository.findAllByPlayerId(userId)).thenReturn(result);

        assertEquals(result, tournamentRegistrationService.getAllRegistrationsByUserId(userId));
    }
    @Test
    public void getAllUnhiddenRegistrationsByUserId_shouldReturnListOfTournamentRegistrations(){
        UUID userId = UUID.randomUUID();
        List<TournamentRegistration> result = TournamentRegistrationFixture.createList();

        when(tournamentRegistrationRepository
                .findAllByPlayerIdAndHiddenFalseOrderByRegisteredOnDesc(userId))
                .thenReturn(result);

        assertEquals(result, tournamentRegistrationService.getAllUnhiddenRegistrationsByUserId(userId));
    }

}

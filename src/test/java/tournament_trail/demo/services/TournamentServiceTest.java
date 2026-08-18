package tournament_trail.demo.services;

import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.exceptions.TournamentDoesNotExist;
import tournament_trail.demo.exceptions.TournamentFullException;
import tournament_trail.demo.exceptions.TournamentHasAlreadyStartedException;
import tournament_trail.demo.exceptions.UserDoesNotExist;
import tournament_trail.demo.fixtures.TournamentFixture;
import tournament_trail.demo.fixtures.dtos.TournamentRequestFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.web.dtos.TournamentOptionResponse;
import tournament_trail.demo.web.dtos.TournamentRequest;
import tournament_trail.demo.web.dtos.TournamentSearchRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TournamentServiceTest {
    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TournamentService tournamentService;

    @Captor
    private ArgumentCaptor<Tournament> captor;

    @Test
    public void getAllUpcomingTournaments_shouldReturnListOfTournaments() {
        List<Tournament> expected = TournamentFixture.createList();

        when(tournamentRepository.findAllByOrderByRegistrationDeadlineAsc()).thenReturn(expected);

        assertEquals(expected, tournamentService.getAllUpcomingTournaments());
    }


    @Test
    public void createTournament_shouldThrowUserDoesNotExistException_whenUserInvalid() {
        UUID userId = UUID.randomUUID();

        when(userService.findById(userId)).thenThrow(UserDoesNotExist.class);

        assertThrows(UserDoesNotExist.class
                , () -> tournamentService.createTournament(new TournamentRequest(), userId));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    public void createTournament_shouldCreateValidTournament() {
        User organiser = UserFixture.createUser();
        TournamentRequest tournamentRequest = TournamentRequestFixture.create();

        when(userService.findById(organiser.getId())).thenReturn(organiser);

        LocalDateTime before = LocalDateTime.now();
        tournamentService.createTournament(tournamentRequest, organiser.getId());

        verify(tournamentRepository).save(captor.capture());
        Tournament result = captor.getValue();

        assertEquals(TournamentRequestFixture.TEST_CITY, result.getCity());
        assertEquals(TournamentRequestFixture.TEST_COUNTRY, result.getCountry());
        assertEquals(TournamentRequestFixture.TEST_VENUE, result.getVenue());
        assertEquals(TournamentRequestFixture.TEST_CURRENCY, result.getCurrency());
        assertEquals(TournamentRequestFixture.TEST_TIME_CONTROL, result.getTimeControl());
        assertEquals(TournamentRequestFixture.TEST_DESCRIPTION, result.getDescription());
        assertEquals(TournamentRequestFixture.TEST_PAYMENT_INSTRUCTIONS, result.getPaymentInstructions());
        assertEquals(TournamentRequestFixture.TEST_PARTICIPATION_REQUIREMENTS, result.getParticipationRequirements());
        assertEquals(TournamentRequestFixture.TEST_EDITION, result.getEdition());
        assertEquals(organiser, result.getOrganiser());
        assertEquals(TournamentRequestFixture.TEST_MAXIMUM_PARTICIPANTS, result.getMaximumParticipants());
        assertEquals(TournamentRequestFixture.TEST_ENTRY_FEE, result.getEntryFee());
        assertEquals(TournamentStatus.DRAFT, result.getStatus());
        assertEquals(tournamentRequest.getStartTime(), result.getStartTime());
        assertEquals(tournamentRequest.getEndTime(), result.getEndTime());
        assertEquals(tournamentRequest.getRegistrationDeadline(), result.getRegistrationDeadline());
        assertFalse(result.getCreatedOn().isBefore(before));
        assertFalse(result.getUpdatedOn().isBefore(before));
    }

    @Test
    public void updateTournamentStatus_shouldThrowTournamentDoesNotExist_whenTournamentInvalid() {
        UUID tournamentId = UUID.randomUUID();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExist.class
                , () -> tournamentService.updateTournamentStatus(
                        tournamentId, TournamentStatus.CANCELLED, UUID.randomUUID(), Role.PLAYER));
    }

    @Test
    public void updateTournamentStatus_shouldThrowAccessDeniedException_whenUserIsNotOwnerOrAdmin() {
        Tournament tournament = TournamentFixture.create();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(AccessDeniedException.class
                , () -> tournamentService.updateTournamentStatus(
                        tournament.getId(), TournamentStatus.CANCELLED, UUID.randomUUID(), Role.PLAYER));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, names = {"DRAFT", "REGISTRATION_CLOSED", "STARTED", "COMPLETED"})
    public void updateTournamentStatus_shouldThrowIllegalArgumentException_whenStatusIsNotPublishedNorCancelledAndUserIsOwner(
            TournamentStatus status) {

        Tournament tournament = TournamentFixture.create();
        UUID ownerId = tournament.getOrganiser().getId();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(IllegalArgumentException.class
                , () -> tournamentService.updateTournamentStatus(
                        tournament.getId(), status, ownerId, Role.PLAYER));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, names = {"DRAFT", "REGISTRATION_CLOSED", "STARTED", "COMPLETED"})
    public void updateTournamentStatus_shouldThrowIllegalArgumentException_whenStatusIsNotPublishedNorCancelledAndUserIsAdmin(
            TournamentStatus status) {

        Tournament tournament = TournamentFixture.create();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(IllegalArgumentException.class
                , () -> tournamentService.updateTournamentStatus(
                        tournament.getId(), status, UUID.randomUUID(), Role.ADMIN));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"DRAFT"})
    public void updateTournamentStatus_shouldThrowIllegalStateException_whenStatusIsNotPublishedNorCancelled(
            TournamentStatus status) {

        Tournament tournament = TournamentFixture.createWithStatus(status);
        UUID ownerId = tournament.getOrganiser().getId();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class
                , () -> tournamentService.updateTournamentStatus(
                        tournament.getId(), TournamentStatus.PUBLISHED, ownerId, Role.PLAYER));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    public void updateTournamentStatus_shouldThrowIllegalStateException_whenStatusIsNotPublishedNorCancelledAndRegistrationHasEnded() {

        Tournament tournament = TournamentFixture.createPaidWithStatusDraft();
        tournament.setRegistrationDeadline(LocalDateTime.now().minusMinutes(1));

        UUID ownerId = tournament.getOrganiser().getId();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class
                , () -> tournamentService.updateTournamentStatus(
                        tournament.getId(), TournamentStatus.PUBLISHED, ownerId, Role.PLAYER));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    public void updateTournamentStatus_shouldUpdateStatusToPublished() {

        Tournament tournament = TournamentFixture.createPaidWithStatusDraft();
        tournament.setRegistrationDeadline(LocalDateTime.now().plusMinutes(1));
        tournament.setStartTime(LocalDateTime.now().plusMinutes(1));
        UUID ownerId = tournament.getOrganiser().getId();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        LocalDateTime before = LocalDateTime.now();
        tournamentService.updateTournamentStatus(
                tournament.getId(), TournamentStatus.PUBLISHED, ownerId, Role.PLAYER);

        assertEquals(TournamentStatus.PUBLISHED, tournament.getStatus());
        assertFalse(tournament.getUpdatedOn().isBefore(before));
    }

    @Test
    public void updateTournamentStatus_shouldThrowIllegalStateException_whenStatusIsNotPublishedNorCancelledAndStartTimeHasStarted() {
        Tournament tournament = TournamentFixture.createPaidWithStatusDraft();
        tournament.setRegistrationDeadline(LocalDateTime.now().plusMinutes(1));
        tournament.setStartTime(LocalDateTime.now().minusMinutes(1));
        UUID ownerId = tournament.getOrganiser().getId();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class
                , () -> tournamentService.updateTournamentStatus(
                        tournament.getId(), TournamentStatus.PUBLISHED, ownerId, Role.PLAYER));
    }

    @Test
    public void updateTournamentStatus_shouldThrowIllegalStateException_whenStatusIsComplete() {
        Tournament tournament = TournamentFixture.createWithStatusCompleted();

        UUID ownerId = tournament.getOrganiser().getId();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class
                , () -> tournamentService.updateTournamentStatus(
                        tournament.getId(), TournamentStatus.CANCELLED, ownerId, Role.PLAYER));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    public void updateTournamentStatus_shouldThrowIllegalStateException_whenStatusIsAlreadyCancelled() {
        Tournament tournament = TournamentFixture.createWithStatusCancelled();

        UUID ownerId = tournament.getOrganiser().getId();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class
                , () -> tournamentService.updateTournamentStatus(
                        tournament.getId(), TournamentStatus.CANCELLED, ownerId, Role.PLAYER));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"CANCELLED", "COMPLETED"})
    public void updateTournamentStatus_cancelTournamentSuccessfully(TournamentStatus status) {
        Tournament tournament = TournamentFixture.createWithStatus(status);
        UUID ownerId = tournament.getOrganiser().getId();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        LocalDateTime before = LocalDateTime.now();
        tournamentService.updateTournamentStatus(
                tournament.getId(), TournamentStatus.CANCELLED, ownerId, Role.PLAYER);

        assertEquals(TournamentStatus.CANCELLED, tournament.getStatus());
        assertFalse(tournament.getUpdatedOn().isBefore(before));
    }


    @Test
    public void editTournament_shouldThrowAccessDeniedException_whenUserIsNotOwnerNorAdmin() {
        Tournament tournament = TournamentFixture.create();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(AccessDeniedException.class
                , () -> tournamentService.editTournament(
                        new TournamentRequest(), tournament.getId(), UUID.randomUUID(), Role.PLAYER));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, names = {"CANCELLED", "COMPLETED"})
    public void editTournament_shouldThrowIllegalStateException_whenUserIsOwnerStatusIsNotCanceledNorCompleted(
            TournamentStatus status) {
        Tournament tournament = TournamentFixture.createWithStatus(status);
        UUID ownerId = tournament.getOrganiser().getId();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class
                , () -> tournamentService.editTournament(
                        new TournamentRequest(), tournament.getId(), ownerId, Role.PLAYER));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, names = {"CANCELLED", "COMPLETED"})
    public void editTournament_shouldThrowIllegalStateException_whenUserIsAdminAndStatusIsNotCanceledNorCompleted(
            TournamentStatus status) {
        Tournament tournament = TournamentFixture.createWithStatus(status);

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class
                , () -> tournamentService.editTournament(
                        new TournamentRequest(), tournament.getId(), UUID.randomUUID(), Role.ADMIN));

        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class
            , mode = EnumSource.Mode.EXCLUDE
            , names = {"CANCELLED", "COMPLETED"})
    public void editTournament_shouldEditTournamentSuccessfully(TournamentStatus status) {

        Tournament tournament = TournamentFixture.createWithStatus(status);
        UUID ownerId = tournament.getOrganiser().getId();
        TournamentRequest tournamentRequest = TournamentRequestFixture.create();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        LocalDateTime before = LocalDateTime.now();
        tournamentService.editTournament(
                tournamentRequest, tournament.getId(), ownerId, Role.PLAYER);

        verify(tournamentRepository).save(captor.capture());
        Tournament result = captor.getValue();

        assertEquals(TournamentRequestFixture.TEST_NAME, result.getName());
        assertEquals(TournamentRequestFixture.TEST_VENUE, result.getVenue());
        assertEquals(TournamentRequestFixture.TEST_CITY, result.getCity());
        assertEquals(TournamentRequestFixture.TEST_COUNTRY, result.getCountry());
        assertEquals(TournamentRequestFixture.TEST_CURRENCY, result.getCurrency());
        assertEquals(tournamentRequest.getRegistrationDeadline(), result.getRegistrationDeadline());
        assertEquals(tournamentRequest.getStartTime(), result.getStartTime());
        assertEquals(tournamentRequest.getEndTime(), result.getEndTime());
        assertEquals(TournamentRequestFixture.TEST_MAXIMUM_PARTICIPANTS, result.getMaximumParticipants());
        assertEquals(TournamentRequestFixture.TEST_ENTRY_FEE, result.getEntryFee());
        assertEquals(TournamentRequestFixture.TEST_PAYMENT_INSTRUCTIONS, result.getPaymentInstructions());
        assertEquals(TournamentRequestFixture.TEST_PARTICIPATION_REQUIREMENTS
                , result.getParticipationRequirements());
        assertEquals(TournamentRequestFixture.TEST_DESCRIPTION, result.getDescription());
        assertEquals(TournamentRequestFixture.TEST_EDITION, result.getEdition());
        assertEquals(TournamentRequestFixture.TEST_TIME_CONTROL, result.getTimeControl());
        assertEquals(tournamentRequest.isRated(), result.isRated());
        assertFalse(result.getUpdatedOn().isBefore(before));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class
            , mode = EnumSource.Mode.EXCLUDE
            , names = {"CANCELLED", "COMPLETED"})
    public void editTournament_shouldEditTournamentSuccessfully_whenUserIsOwner(TournamentStatus status) {

        Tournament tournament = TournamentFixture.createWithStatus(status);
        TournamentRequest tournamentRequest = TournamentRequestFixture.create();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        LocalDateTime before = LocalDateTime.now();
        tournamentService.editTournament(
                tournamentRequest, tournament.getId(), UUID.randomUUID(), Role.ADMIN);

        verify(tournamentRepository).save(captor.capture());
        Tournament result = captor.getValue();

        assertEquals(TournamentRequestFixture.TEST_NAME, result.getName());
        assertEquals(TournamentRequestFixture.TEST_VENUE, result.getVenue());
        assertEquals(TournamentRequestFixture.TEST_CITY, result.getCity());
        assertEquals(TournamentRequestFixture.TEST_COUNTRY, result.getCountry());
        assertEquals(TournamentRequestFixture.TEST_CURRENCY, result.getCurrency());
        assertEquals(tournamentRequest.getRegistrationDeadline(), result.getRegistrationDeadline());
        assertEquals(tournamentRequest.getStartTime(), result.getStartTime());
        assertEquals(tournamentRequest.getEndTime(), result.getEndTime());
        assertEquals(TournamentRequestFixture.TEST_MAXIMUM_PARTICIPANTS, result.getMaximumParticipants());
        assertEquals(TournamentRequestFixture.TEST_ENTRY_FEE, result.getEntryFee());
        assertEquals(TournamentRequestFixture.TEST_PAYMENT_INSTRUCTIONS, result.getPaymentInstructions());
        assertEquals(TournamentRequestFixture.TEST_PARTICIPATION_REQUIREMENTS
                , result.getParticipationRequirements());
        assertEquals(TournamentRequestFixture.TEST_DESCRIPTION, result.getDescription());
        assertEquals(TournamentRequestFixture.TEST_EDITION, result.getEdition());
        assertEquals(TournamentRequestFixture.TEST_TIME_CONTROL, result.getTimeControl());
        assertEquals(tournamentRequest.isRated(), result.isRated());
        assertFalse(result.getUpdatedOn().isBefore(before));
    }

    @Test
    public void mapToTournamentRequest_shouldMapToTournamentRequestSuccessfully() {
        Tournament tournament = TournamentFixture.create();

        TournamentRequest result = tournamentService.mapToTournamentRequest(tournament);

        assertEquals(tournament.getName(), result.getName());
        assertEquals(tournament.getVenue(), result.getVenue());
        assertEquals(tournament.getRegistrationDeadline(), result.getRegistrationDeadline());
        assertEquals(tournament.getTimeControl(), result.getTimeControl());
        assertEquals(tournament.getCountry(), result.getCountry());
        assertEquals(tournament.getCity(), result.getCity());
        assertEquals(tournament.getEntryFee(), result.getEntryFee());
        assertEquals(tournament.getCurrency(), result.getCurrency());
        assertEquals(tournament.getStartTime(), result.getStartTime());
        assertEquals(tournament.getEndTime(), result.getEndTime());
        assertEquals(tournament.getEdition(), result.getEdition());
        assertEquals(tournament.getDescription(), result.getDescription());
        assertEquals(tournament.isRated(), result.isRated());
        assertEquals(tournament.getMaximumParticipants(), result.getMaximumParticipants());
        assertEquals(tournament.getParticipationRequirements(), result.getParticipationRequirements());
        assertEquals(tournament.getPaymentInstructions(), result.getPaymentInstructions());
    }

    @Test
    public void updateTournamentStatuses_shouldUpdateAllStatusesSuccessfully() {
        LocalDateTime alreadyPassed = LocalDateTime.now().minusMinutes(1);

        Tournament publishedTournament = TournamentFixture.create();
        publishedTournament.setRegistrationDeadline(alreadyPassed);

        Tournament startedTournament = TournamentFixture.createWithStatusStarted();
        startedTournament.setEndTime(alreadyPassed);

        Tournament registrationClosedTournament = TournamentFixture.createWithStatusRegistrationClosed();
        registrationClosedTournament.setStartTime(alreadyPassed);

        List<Tournament> tournaments =
                List.of(publishedTournament, startedTournament, registrationClosedTournament);

        LocalDateTime before = LocalDateTime.now();
        when(tournamentRepository.findAllByStatusIn(List.of(
                TournamentStatus.PUBLISHED,
                TournamentStatus.STARTED,
                TournamentStatus.REGISTRATION_CLOSED
        ))).thenReturn(tournaments);

        tournamentService.updateTournamentStatuses();

        Tournament publishedTournamentUpdated = tournaments.get(0);
        Tournament startedTournamentUpdated = tournaments.get(1);
        Tournament registrationClosedTournamentUpdated = tournaments.get(2);

        assertEquals(TournamentStatus.REGISTRATION_CLOSED, publishedTournamentUpdated.getStatus());
        assertFalse(publishedTournamentUpdated.getUpdatedOn().isBefore(before));

        assertEquals(TournamentStatus.COMPLETED, startedTournamentUpdated.getStatus());
        assertFalse(startedTournamentUpdated.getUpdatedOn().isBefore(before));

        assertEquals(TournamentStatus.STARTED, registrationClosedTournamentUpdated.getStatus());
        assertFalse(registrationClosedTournamentUpdated.getUpdatedOn().isBefore(before));
    }

    @Test
    public void updateTournamentStatuses_shouldNotUpdateStatuses_whenDatesHaveNotPassedYet() {
        LocalDateTime future = LocalDateTime.now().plusMinutes(1);

        Tournament publishedTournament = TournamentFixture.createWithStatusPublished();
        publishedTournament.setRegistrationDeadline(future);
        LocalDateTime publishedTournamentUpdatedOn = publishedTournament.getUpdatedOn();

        Tournament startedTournament = TournamentFixture.createWithStatusStarted();
        startedTournament.setEndTime(future);
        LocalDateTime startedTournamentUpdatedOn = startedTournament.getUpdatedOn();

        Tournament registrationClosedTournament =
                TournamentFixture.createWithStatusRegistrationClosed();
        registrationClosedTournament.setStartTime(future);
        LocalDateTime registrationTournamentClosedUpdatedOn = registrationClosedTournament.getUpdatedOn();

        List<Tournament> tournaments = List.of(
                publishedTournament,
                startedTournament,
                registrationClosedTournament
        );

        when(tournamentRepository.findAllByStatusIn(List.of(
                TournamentStatus.PUBLISHED,
                TournamentStatus.STARTED,
                TournamentStatus.REGISTRATION_CLOSED
        ))).thenReturn(tournaments);

        tournamentService.updateTournamentStatuses();

        assertEquals(TournamentStatus.PUBLISHED, publishedTournament.getStatus());
        assertEquals(publishedTournamentUpdatedOn, publishedTournament.getUpdatedOn());

        assertEquals(TournamentStatus.STARTED, startedTournament.getStatus());
        assertEquals(startedTournamentUpdatedOn, startedTournament.getUpdatedOn());

        assertEquals(TournamentStatus.REGISTRATION_CLOSED, registrationClosedTournament.getStatus());
        assertEquals(registrationTournamentClosedUpdatedOn, registrationClosedTournament.getUpdatedOn());
    }

    @Test
    public void getTournamentOptionLabel_shouldReturnEmptyString_whenInputIsNull() {
        assertEquals("", tournamentService.getTournamentOptionLabel(null));
    }

    @Test
    public void getTournamentOptionLabel_shouldThrowTournamentDoesNotExist_whenInvalidTournament() {
        UUID tournamentId = UUID.randomUUID();
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());
        assertThrows(TournamentDoesNotExist.class
                , () -> tournamentService.getTournamentOptionLabel(tournamentId));
    }

    @Test
    public void getTournamentOptionLabel_shouldReturnTournamentName() {
        Tournament tournament = TournamentFixture.create();

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));

        assertEquals(TournamentFixture.TEST_NAME, tournamentService.getTournamentOptionLabel(tournament.getId()));
    }

    @Test
    public void searchTournaments_shouldReturnRepositoryResult() {
        TournamentSearchRequest request = new TournamentSearchRequest();

        List<Tournament> expected = TournamentFixture.createList();

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "startTime");

        when(tournamentRepository.findAll(
                ArgumentMatchers.<Specification<Tournament>>any(),
                eq(expectedSort)))
                .thenReturn(expected);

        List<Tournament> result = tournamentService.searchTournaments(request);

        assertEquals(expected, result);

        verify(tournamentRepository).findAll(
                ArgumentMatchers.<Specification<Tournament>>any(),
                eq(expectedSort));
    }

    @Test
    public void searchTournaments_shouldReturnRepositoryResult_whenAllFiltersAreProvided() {
        TournamentSearchRequest request = new TournamentSearchRequest();
        request.setName("  Sofia Open  ");
        request.setCountry("  Bulgaria  ");
        request.setCity("  Sofia  ");
        request.setTimeControl(TournamentRequestFixture.TEST_TIME_CONTROL);
        request.setRated(true);

        List<Tournament> expected = TournamentFixture.createList();
        Sort expectedSort = Sort.by(Sort.Direction.ASC, "startTime");

        when(tournamentRepository.findAll(
                ArgumentMatchers.<Specification<Tournament>>any(),
                eq(expectedSort)))
                .thenReturn(expected);

        List<Tournament> result = tournamentService.searchTournaments(request);

        assertEquals(expected, result);

        verify(tournamentRepository).findAll(
                ArgumentMatchers.<Specification<Tournament>>any(),
                eq(expectedSort));
    }

    @Test
    public void searchTournaments_shouldIgnoreBlankStringFilters() {
        TournamentSearchRequest request = new TournamentSearchRequest();
        request.setName("   ");
        request.setCountry("");
        request.setCity("   ");
        request.setTimeControl(null);
        request.setRated(null);

        List<Tournament> expected = TournamentFixture.createList();
        Sort expectedSort = Sort.by(Sort.Direction.ASC, "startTime");

        when(tournamentRepository.findAll(
                ArgumentMatchers.<Specification<Tournament>>any(),
                eq(expectedSort)))
                .thenReturn(expected);

        List<Tournament> result = tournamentService.searchTournaments(request);

        assertEquals(expected, result);

        verify(tournamentRepository).findAll(
                ArgumentMatchers.<Specification<Tournament>>any(),
                eq(expectedSort));
    }

    @Test
    public void canEditTournament_shouldReturnTrue_whenUserIsAdmin() {
        Tournament tournament = TournamentFixture.create();

        assertTrue(tournamentService.canEditTournament(tournament, UUID.randomUUID(), Role.ADMIN));
    }

    @Test
    public void canEditTournament_shouldReturnTrue_whenUserIsOwner() {
        Tournament tournament = TournamentFixture.create();

        assertTrue(tournamentService.canEditTournament(tournament, tournament.getOrganiser().getId(), Role.PLAYER));
    }

    @Test
    public void canEditTournament_shouldReturnFalse_whenUserIsNotOwnerNorAdmin() {
        Tournament tournament = TournamentFixture.create();

        assertFalse(tournamentService.canEditTournament(tournament, UUID.randomUUID(), Role.PLAYER));
    }

    @Test
    public void validateTournamentNotFull_shouldThrowTournamentFullException_whenTournamentIsFull() {
        assertThrows(TournamentFullException.class
                , () -> tournamentService.validateTournamentNotFull(10, 10));
    }

    @Test
    public void validateTournamentNotFull_shouldNotThrowException_whenTournamentIsNotFull() {
        assertDoesNotThrow(() -> tournamentService.validateTournamentNotFull(11, 10));
    }

    @Test
    public void validateTournamentConditions_shouldThrowIllegalStateException_whenTournamentIsCancelled() {
        Tournament tournament = TournamentFixture.createWithStatusCancelled();

        assertThrows(IllegalStateException.class
                , () -> tournamentService.validateTournamentConditions(tournament, LocalDateTime.now()));
    }

    @Test
    public void validateTournamentConditions_shouldThrowAccessDeniedException_whenStatusIsRegistrationClosed() {
        Tournament tournament = TournamentFixture.createWithStatusRegistrationClosed();

        assertThrows(AccessDeniedException.class
                , () -> tournamentService.validateTournamentConditions(tournament, LocalDateTime.now()));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, names = {"DRAFT", "STARTED", "COMPLETED"})
    public void validateTournamentConditions_shouldThrowAccessDeniedException_whenStatusIsNotPublished(
            TournamentStatus status) {

        LocalDateTime now = LocalDateTime.now();

        Tournament tournament = TournamentFixture.createWithStatus(status);
        tournament.setStartTime(now.plusDays(1));
        tournament.setRegistrationDeadline(now.plusHours(2));

        assertThrows(AccessDeniedException.class,
                () -> tournamentService.validateTournamentConditions(tournament, now));
    }

    @Test
    public void validateTournamentConditions_shouldThrowTournamentHasAlreadyStartedException_whenTournamentHasStarted() {
        Tournament tournament = TournamentFixture.createPaidWithStatusPublished();
        tournament.setStartTime(LocalDateTime.now().minusMinutes(1));

        assertThrows(TournamentHasAlreadyStartedException.class
                , () -> tournamentService.validateTournamentConditions(tournament, LocalDateTime.now()));
    }


    @Test
    public void validateTournamentConditions_shouldThrowAccessDeniedException_whenRegistrationHasEnded() {
        Tournament tournament = TournamentFixture.createPaidWithStatusPublished();
        tournament.setStartTime(LocalDateTime.now().plusMinutes(1));
        tournament.setRegistrationDeadline(LocalDateTime.now().minusMinutes(1));

        assertThrows(AccessDeniedException.class
                , () -> tournamentService.validateTournamentConditions(tournament, LocalDateTime.now()));
    }

    @Test
    public void validateTournamentConditions_shouldNotThrowException_whenTournamentIsValid() {
        LocalDateTime now = LocalDateTime.now();

        Tournament tournament = TournamentFixture.createWithStatusPublished();
        tournament.setStartTime(now.plusDays(1));
        tournament.setRegistrationDeadline(now.plusHours(2));

        assertDoesNotThrow(() ->
                tournamentService.validateTournamentConditions(tournament, now));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "a", " A ", "  b  "})
    public void searchTournamentOptions_shouldReturnEmptyList_whenQueryIsNullBlankOrTooShort(
            String query) {

        List<TournamentOptionResponse> result =
                tournamentService.searchTournamentOptions(query);

        assertTrue(result.isEmpty());

        verify(tournamentRepository, never()).searchTournamentOptions(
                anyString(),
                any(TournamentStatus.class),
                any(LocalDateTime.class),
                any(Pageable.class)
        );
    }

    @Test
    public void searchTournamentOptions_shouldReturnTournamentOptions_whenQueryIsValid() {
        String query = "  SOFIA  ";

        List<TournamentOptionResponse> expected = List.of(
                mock(TournamentOptionResponse.class),
                mock(TournamentOptionResponse.class));

        when(tournamentRepository.searchTournamentOptions(
                eq("sofia"),
                eq(TournamentStatus.PUBLISHED),
                any(LocalDateTime.class),
                eq(PageRequest.of(0, 15))))
                .thenReturn(expected);

        List<TournamentOptionResponse> result =
                tournamentService.searchTournamentOptions(query);

        assertEquals(expected, result);

        verify(tournamentRepository).searchTournamentOptions(
                eq("sofia"),
                eq(TournamentStatus.PUBLISHED),
                any(LocalDateTime.class),
                eq(PageRequest.of(0, 15)));
    }

}

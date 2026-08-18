package tournament_trail.demo.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import tournament_trail.demo.exceptions.TournamentDoesNotExist;
import tournament_trail.demo.exceptions.TravelGroupDoesNotExistException;
import tournament_trail.demo.exceptions.UserDoesNotExist;
import tournament_trail.demo.fixtures.TournamentFixture;
import tournament_trail.demo.fixtures.TravelGroupFixture;
import tournament_trail.demo.fixtures.dtos.TravelGroupRequestFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.TravelGroupRepository;
import tournament_trail.demo.web.dtos.TravelGroupRequest;
import tournament_trail.demo.web.dtos.TravelGroupSearchRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TravelGroupServiceTest {
    @Mock
    private TravelGroupRepository travelGroupRepository;

    @Mock
    private TournamentService tournamentService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TravelGroupService travelGroupService;

    @Captor
    private ArgumentCaptor<TravelGroup> captor;


    @Test
    public void findById_shouldThrowTravelGroupDoesNotExistException_whenInvalidTravelGroup() {
        UUID travelGroupId = UUID.randomUUID();

        when(travelGroupRepository.findById(travelGroupId)).thenReturn(Optional.empty());

        assertThrows(TravelGroupDoesNotExistException.class
                , () -> travelGroupService.findById(travelGroupId));
    }

    @Test
    public void createTravelGroup_shouldThrowUserDoesNotExist_whenInvalidUser() {
        UUID userId = UUID.randomUUID();
        TravelGroupRequest request = new TravelGroupRequest();

        when(userService.findById(userId)).thenThrow(UserDoesNotExist.class);

        assertThrows(UserDoesNotExist.class
                , () -> travelGroupService.createTravelGroup(request, userId));
    }

    @Test
    public void createTravelGroup_shouldThrowTournamentDoesNotExist_whenTournamentInvalid() {
        User user = UserFixture.createUser();
        TravelGroupRequest request = TravelGroupRequestFixture.create();

        when(userService.findById(user.getId())).thenReturn(user);
        when(tournamentService.findById(request.getTournamentId())).thenThrow(TournamentDoesNotExist.class);

        assertThrows(TournamentDoesNotExist.class
                , () -> travelGroupService.createTravelGroup(request, user.getId()));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class
            , names = {"DRAFT", "REGISTRATION_CLOSED", "STARTED", "COMPLETED", "CANCELLED"})
    public void createTravelGroup_shouldThrowIllegalStateException_whenTournamentStatusIsNotPublished(
            TournamentStatus status) {

        Tournament tournament = TournamentFixture.createWithStatus(status);
        User user = UserFixture.createUser();
        TravelGroupRequest request = TravelGroupRequestFixture.createWithTournament(tournament.getId());

        when(userService.findById(user.getId())).thenReturn(user);
        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        assertThrows(IllegalStateException.class
                , () -> travelGroupService.createTravelGroup(request, user.getId()));
    }

    @Test
    public void createTravelGroup_shouldThrowIllegalStateException_whenTournamentHasStarted() {
        Tournament tournament = TournamentFixture.createTournamentWithStartTime(LocalDateTime.now());
        User user = UserFixture.createUser();
        TravelGroupRequest request = TravelGroupRequestFixture.createWithTournament(tournament.getId());

        when(userService.findById(user.getId())).thenReturn(user);
        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        assertThrows(IllegalStateException.class
                , () -> travelGroupService.createTravelGroup(request, user.getId()));
    }

    @Test
    public void createTravelGroup_shouldThrowIllegalStateException_whenDepartureTimeIsAfterTournamentStart() {
        User user = UserFixture.createUser();

        LocalDateTime tournamentStart = LocalDateTime.now().plusDays(5);
        Tournament tournament = TournamentFixture.createTournamentWithStartTime(tournamentStart);

        TravelGroupRequest request = TravelGroupRequestFixture
                .createWithTournament(tournament.getId());

        request.setDepartureTime(tournamentStart.plusHours(1));

        when(userService.findById(user.getId())).thenReturn(user);
        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> travelGroupService.createTravelGroup(request, user.getId())
        );

        assertEquals("Departure time must be before the tournament starts.", exception.getMessage());

        verify(travelGroupRepository, never()).save(any(TravelGroup.class));
    }

    @Test
    public void createTravelGroup_shouldCreateValidTravelGroup() {
        User owner = UserFixture.createUser();
        Tournament tournament = TournamentFixture.create();
        TravelGroupRequest request = TravelGroupRequestFixture.createWithTournament(tournament.getId());

        when(userService.findById(owner.getId())).thenReturn(owner);
        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        travelGroupService.createTravelGroup(request, owner.getId());

        verify(travelGroupRepository).save(captor.capture());
        TravelGroup result = captor.getValue();

        assertEquals(TravelGroupRequestFixture.TEST_CITY, result.getDepartureCity());
        assertEquals(TravelGroupRequestFixture.TEST_COUNTRY, result.getDepartureCountry());
        assertEquals(TravelGroupRequestFixture.TEST_MEETING_POINT, result.getMeetingPoint());
        assertEquals(TravelGroupRequestFixture.TEST_NAME, result.getName());
        assertEquals(TravelGroupRequestFixture.TEST_DESCRIPTION, result.getDescription());
        assertEquals(TravelGroupRequestFixture.TEST_MAXIMUM_MEMBERS, result.getMaximumMembers());
        assertEquals(TravelGroupRequestFixture.TEST_CURRENCY, result.getCurrency());
        assertEquals(TravelGroupRequestFixture.TEST_TRANSPORTATION_TYPE, result.getTransportationType());
        assertEquals(TravelGroupRequestFixture.TEST_DEPARTURE_DATE, result.getDepartureTime());
        assertEquals(TravelGroupRequestFixture.TEST_ESTIMATED_COST, result.getEstimatedCost());
        assertEquals(TravelGroupStatus.OPEN, result.getStatus());
        assertEquals(tournament, result.getTournament());
        assertEquals(owner, result.getOwner());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"PLAYER", "ORGANISER"})
    public void cancel_shouldCancelTravelGroupSuccessfully_whenUserIsOwner(Role role) {
        TravelGroup travelGroup = TravelGroupFixture.create();
        LocalDateTime before = LocalDateTime.now();

        when(travelGroupRepository.findById(travelGroup.getId())).thenReturn(Optional.of(travelGroup));

        travelGroupService.cancel(travelGroup.getId(), travelGroup.getOwner().getId(), role);
        verify(travelGroupRepository).save(captor.capture());

        TravelGroup result = captor.getValue();

        assertEquals(TravelGroupStatus.CANCELLED, result.getStatus());
        assertFalse(result.getUpdatedOn().isBefore(before));
    }

    @Test
    public void cancel_shouldCancelTravelGroupSuccessfully_whenUserIsAdmin() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        LocalDateTime before = travelGroup.getUpdatedOn();

        when(travelGroupRepository.findById(travelGroup.getId())).thenReturn(Optional.of(travelGroup));

        travelGroupService.cancel(travelGroup.getId(), UUID.randomUUID(), Role.ADMIN);

        verify(travelGroupRepository).save(captor.capture());

        TravelGroup result = captor.getValue();

        assertEquals(TravelGroupStatus.CANCELLED, result.getStatus());
        assertTrue(before.isBefore(result.getUpdatedOn()));

    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"PLAYER", "ORGANISER"})
    public void cancel_shouldThrowAccessDeniedException_whenNotOwnerNorAdmin(Role role) {
        TravelGroup travelGroup = TravelGroupFixture.create();

        when(travelGroupRepository.findById(travelGroup.getId())).thenReturn(Optional.of(travelGroup));

        assertThrows(AccessDeniedException.class
                , () -> travelGroupService.cancel(travelGroup.getId(), UUID.randomUUID(), role));

        verify(travelGroupRepository, never()).save(any(TravelGroup.class));
    }

    @Test
    public void cancel_shouldThrowIllegalStateException_whenTravelGroupIsAlreadyCancelled() {
        TravelGroup travelGroup = TravelGroupFixture.createWithCancelledStatus();

        when(travelGroupRepository.findById(travelGroup.getId())).thenReturn(Optional.of(travelGroup));

        assertThrows(IllegalStateException.class
                , () -> travelGroupService.cancel(
                        travelGroup.getId(), travelGroup.getOwner().getId(), Role.ADMIN));

        verify(travelGroupRepository, never()).save(any(TravelGroup.class));
    }

    @Test
    public void mapToTravelGroupRequest_shouldReturnValidTravelGroupRequest() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        TravelGroupRequest result = travelGroupService.mapToTravelGroupRequest(travelGroup);

        assertEquals(TravelGroupFixture.TEST_NAME, result.getName());
        assertEquals(TravelGroupFixture.TEST_COUNTRY, result.getDepartureCountry());
        assertEquals(TravelGroupFixture.TEST_CITY, result.getDepartureCity());
        assertEquals(TravelGroupFixture.TEST_DEPARTURE_DATE, result.getDepartureTime());
        assertEquals(TravelGroupFixture.TEST_MAXIMUM_MEMBERS, result.getMaximumMembers());
        assertEquals(TravelGroupFixture.TEST_DESCRIPTION, result.getDescription());
        assertEquals(TravelGroupFixture.TEST_CURRENCY, result.getCurrency());
        assertEquals(TravelGroupFixture.TEST_MEETING_POINT, result.getMeetingPoint());
        assertEquals(TravelGroupFixture.TEST_TRANSPORTATION_TYPE, result.getTransportationType());
        assertEquals(TravelGroupFixture.TEST_ESTIMATED_COST, result.getEstimatedCost());
        assertEquals(travelGroup.getTournament().getId(), result.getTournamentId());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ORGANISER", "PLAYER"})
    public void updateTravelGroup_shouldUpdateTravelGroupSuccessfully_whenUserIsOwner(Role role) {
        TravelGroup travelGroup = TravelGroupFixture.create();
        TravelGroupRequest travelGroupRequest = TravelGroupRequestFixture.create();

        when(travelGroupRepository.findById(travelGroup.getId())).thenReturn(Optional.of(travelGroup));

        when(tournamentService.findById(travelGroupRequest.getTournamentId()))
                .thenReturn(travelGroup.getTournament());

        LocalDateTime before = LocalDateTime.now();

        travelGroupService.updateTravelGroup(
                travelGroup.getId(), travelGroupRequest, travelGroup.getOwner().getId(), role);

        verify(travelGroupRepository).save(captor.capture());
        TravelGroup result = captor.getValue();

        assertEquals(TravelGroupRequestFixture.TEST_NAME, result.getName());
        assertEquals(TravelGroupRequestFixture.TEST_COUNTRY, result.getDepartureCountry());
        assertEquals(TravelGroupRequestFixture.TEST_CITY, result.getDepartureCity());
        assertEquals(TravelGroupRequestFixture.TEST_DEPARTURE_DATE, result.getDepartureTime());
        assertEquals(TravelGroupRequestFixture.TEST_MAXIMUM_MEMBERS, result.getMaximumMembers());
        assertEquals(TravelGroupRequestFixture.TEST_DESCRIPTION, result.getDescription());
        assertEquals(TravelGroupRequestFixture.TEST_CURRENCY, result.getCurrency());
        assertEquals(TravelGroupRequestFixture.TEST_MEETING_POINT, result.getMeetingPoint());
        assertEquals(TravelGroupRequestFixture.TEST_TRANSPORTATION_TYPE, result.getTransportationType());
        assertEquals(TravelGroupRequestFixture.TEST_ESTIMATED_COST, result.getEstimatedCost());
        assertEquals(travelGroup.getTournament(), result.getTournament());
        assertFalse(result.getUpdatedOn().isBefore(before));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ORGANISER", "PLAYER"})
    public void updateTravelGroup_shouldThrowAccessDeniedException_whenUserIsNotOwnerNorAdmin(Role role) {
        TravelGroup travelGroup = TravelGroupFixture.create();
        TravelGroupRequest travelGroupRequest = TravelGroupRequestFixture.create();

        when(travelGroupRepository.findById(travelGroup.getId())).thenReturn(Optional.of(travelGroup));

        assertThrows(AccessDeniedException.class
        , ()->travelGroupService.updateTravelGroup(
                travelGroup.getId(), travelGroupRequest, UUID.randomUUID(), role ));

        verify(travelGroupRepository, never()).save(any(TravelGroup.class));
    }

    @Test
    public void updateTravelGroup_shouldUpdateTravelGroupSuccessfully_whenUserIsAdmin() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        TravelGroupRequest travelGroupRequest = TravelGroupRequestFixture.create();

        when(travelGroupRepository.findById(travelGroup.getId()))
                .thenReturn(Optional.of(travelGroup));

        when(tournamentService.findById(travelGroupRequest.getTournamentId()))
                .thenReturn(travelGroup.getTournament());

        travelGroupService.updateTravelGroup(
                travelGroup.getId(), travelGroupRequest, UUID.randomUUID(), Role.ADMIN);

        verify(travelGroupRepository).save(any(TravelGroup.class));
    }

    @Test
    public void getTravelGroupsByUser_shouldReturnListOfTravelGroups(){
        List<TravelGroup> result = TravelGroupFixture.creteListWithSameOwner();
        UUID ownerId = result.get(0).getOwner().getId();
        when(travelGroupRepository.findAllByOwnerIdOrderByDepartureTimeAsc(ownerId))
                .thenReturn(result);

        assertEquals(result, travelGroupService.getTravelGroupsByUser(ownerId));
    }

    @Test
    public void searchTravelGroups_shouldReturnRepositoryResult() {
        TravelGroupSearchRequest searchRequest = new TravelGroupSearchRequest();

        List<TravelGroup> expected = TravelGroupFixture.createList();

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "departureTime");

        when(travelGroupRepository.findAll(
                ArgumentMatchers.<Specification<TravelGroup>>any(), eq(expectedSort)))
                .thenReturn(expected);

        List<TravelGroup> result = travelGroupService.searchTravelGroups(searchRequest);

        assertEquals(expected, result);

        verify(travelGroupRepository).findAll(
                ArgumentMatchers.<Specification<TravelGroup>>any(), eq(expectedSort));
    }
}

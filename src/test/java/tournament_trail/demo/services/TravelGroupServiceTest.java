package tournament_trail.demo.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.exceptions.TournamentDoesNotExist;
import tournament_trail.demo.exceptions.TravelGroupDoesNotExistException;
import tournament_trail.demo.exceptions.UserDoesNotExist;
import tournament_trail.demo.fixtures.TournamentFixture;
import tournament_trail.demo.fixtures.TravelGroupRequestFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.TravelGroupRepository;
import tournament_trail.demo.web.dtos.TravelGroupRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
    public void findById_shouldThrowTravelGroupDoesNotExistException() {
        UUID travelGroupId = UUID.randomUUID();

        when(travelGroupRepository.findById(travelGroupId)).thenReturn(Optional.empty());

        assertThrows(TravelGroupDoesNotExistException.class
                , () -> travelGroupService.findById(travelGroupId));
    }

    @Test
    public void createTravelGroup_shouldThrowUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        TravelGroupRequest request = new TravelGroupRequest();

        when(userService.findById(userId)).thenThrow(UserDoesNotExist.class);

        assertThrows(UserDoesNotExist.class
                , () -> travelGroupService.createTravelGroup(request, userId));

    }

    @Test
    public void createTravelGroup_shouldThrowTournamentDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID tournamentID = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        User user = UserFixture.createUser(userId);
        TravelGroupRequest request = TravelGroupRequestFixture.create(requestId, tournamentID);

        when(userService.findById(userId)).thenReturn(user);
        when(tournamentService.findById(tournamentID)).thenThrow(TournamentDoesNotExist.class);

        assertThrows(TournamentDoesNotExist.class
                , () -> travelGroupService.createTravelGroup(request, userId));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class
            , names = {"DRAFT", "REGISTRATION_CLOSED", "STARTED", "COMPLETED", "CANCELLED"})
    public void createTravelGroup_shouldThrowIllegalStateException_whenTournamentStatusIsNotPublished(
            TournamentStatus status) {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Tournament tournament = TournamentFixture.createTournamentWithStatus(tournamentId, status);
        User user = UserFixture.createUser(userId);
        TravelGroupRequest request = TravelGroupRequestFixture.create(UUID.randomUUID(), tournamentId);

        when(userService.findById(userId)).thenReturn(user);
        when(tournamentService.findById(tournamentId)).thenReturn(tournament);

        assertThrows(IllegalStateException.class
                , () -> travelGroupService.createTravelGroup(request, userId));
    }

    @Test
    public void createTravelGroup_shouldThrowIllegalArgumentException_whenTournamentHasStarted() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Tournament tournament = TournamentFixture.createTournamentWithStartTime(
                tournamentId, LocalDateTime.now());
        User user = UserFixture.createUser(userId);
        TravelGroupRequest request = TravelGroupRequestFixture.create(UUID.randomUUID(), tournamentId);

        when(userService.findById(userId)).thenReturn(user);
        when(tournamentService.findById(tournamentId)).thenReturn(tournament);

        assertThrows(IllegalStateException.class
                , () -> travelGroupService.createTravelGroup(request, userId));
    }

    @Test
    public void createTravelGroup_shouldThrowIllegalStateException_whenDepartureTimeIsAfterTournamentStart() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Tournament tournament = TournamentFixture.createTournamentWithStartTime(
                tournamentId, LocalDateTime.now());
        User user = UserFixture.createUser(userId);
        TravelGroupRequest request = TravelGroupRequestFixture.create(UUID.randomUUID(), tournamentId);

        when(userService.findById(userId)).thenReturn(user);
        when(tournamentService.findById(tournamentId)).thenReturn(tournament);

        assertThrows(IllegalStateException.class
                , () -> travelGroupService.createTravelGroup(request, userId));
    }
    // @Transactional

    //    public TravelGroup createTravelGroup(TravelGroupRequest request, UUID ownerId) {
    //        User owner = userService.findById(ownerId);
    //
    //        Tournament tournament = tournamentService.findById(request.getTournamentId());
    //
    //        validateTournamentForTravelGroupCreation(tournament);
    //        validateDepartureTime(request, tournament);
    //
    //        TravelGroup travelGroup = TravelGroup.builder()
    //                .name(request.getName())
    //                .departureCountry(request.getDepartureCountry())
    //                .departureCity(request.getDepartureCity())
    //                .departureTime(request.getDepartureTime())
    //                .maximumMembers(request.getMaximumMembers())
    //                .description(request.getDescription())
    //                .status(TravelGroupStatus.OPEN)
    //                .tournament(tournament)
    //                .meetingPoint(request.getMeetingPoint())
    //                .transportationType(request.getTransportationType())
    //                .estimatedCost(request.getEstimatedCost())
    //                .currency(request.getCurrency())
    //                .owner(owner)
    //                .createdOn(LocalDateTime.now())
    //                .updatedOn(LocalDateTime.now())
    //                .build();
    //        return travelGroupRepository.save(travelGroup);
    //    }
}

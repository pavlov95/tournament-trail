package tournament_trail.demo.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import tournament_trail.demo.entities.enums.TravelRequestStatus;
import tournament_trail.demo.exceptions.AlreadyPartOfGroupException;
import tournament_trail.demo.exceptions.RequestAlreadyExistsException;
import tournament_trail.demo.exceptions.TravelGroupFullException;
import tournament_trail.demo.fixtures.TravelGroupFixture;
import tournament_trail.demo.fixtures.TravelRequestFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.TravelRequestRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TravelRequestServiceTest {
    @Mock
    private TravelRequestRepository travelRequestRepository;

    @Mock
    private UserService userService;

    @Captor
    private ArgumentCaptor<TravelRequest> captor;

    @InjectMocks
    private TravelRequestService travelRequestService;

    @Test
    public void findAcceptedRequests_shouldReturnAllAcceptedRequests() {
        List<TravelRequest> result = TravelRequestFixture.createList();
        UUID userId = UUID.randomUUID();
        when(travelRequestRepository.findAllByApplicantIdAndStatus(
                userId, TravelRequestStatus.APPROVED))
                .thenReturn(result);

        assertEquals(result, travelRequestService.findAcceptedRequests(userId));
    }

    @Test
    public void createTravelRequest_shouldThrowAlreadyPartOfTravelGroupException_whenUserIsOwner() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        UUID userId = travelGroup.getOwner().getId();
        String message = "Test";

        assertThrows(AlreadyPartOfGroupException.class
                , () -> travelRequestService.createTravelRequest(travelGroup, userId, message));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }

    @ParameterizedTest
    @EnumSource(value = TravelGroupStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "OPEN")
    public void createTravelRequest_shouldThrowAccessDeniedException_whenTravelGroupStatusNotOpen(
            TravelGroupStatus status) {
        TravelGroup travelGroup = TravelGroupFixture.create();
        travelGroup.setStatus(status);
        String message = "Test";

        assertThrows(AccessDeniedException.class
                , () -> travelRequestService.createTravelRequest(
                        travelGroup, UUID.randomUUID(), message));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }


    @Test
    public void createTravelRequest_shouldThrowTravelGroupFullException_whenTravelGroupIsFull() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        String message = "Test";

        when(travelRequestRepository.countByTravelGroupIdAndStatus(
                travelGroup.getId(), TravelRequestStatus.APPROVED))
                .thenReturn(3);

        assertThrows(TravelGroupFullException.class
                , () -> travelRequestService.createTravelRequest(
                        travelGroup, UUID.randomUUID(), message));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }

    @Test
    public void createTravelRequest_shouldThrowRequestAlreadyExistsException_whenRequestsExists() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        UUID userId = UUID.randomUUID();
        String message = "Test";

        when(travelRequestRepository.countByTravelGroupIdAndStatus(
                travelGroup.getId(), TravelRequestStatus.APPROVED))
                .thenReturn(2);

        when(travelRequestRepository.existsByTravelGroupIdAndApplicantId(
                travelGroup.getId(), userId))
                .thenReturn(true);

        assertThrows(RequestAlreadyExistsException.class
                , () -> travelRequestService.createTravelRequest(
                        travelGroup, userId, message));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }

    @Test
    public void createTravelRequest_shouldCreateSuccessfully() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        User user = UserFixture.createUser();
        String message = "Test";

        when(travelRequestRepository.countByTravelGroupIdAndStatus(
                travelGroup.getId(), TravelRequestStatus.APPROVED))
                .thenReturn(2);

        when(travelRequestRepository.existsByTravelGroupIdAndApplicantId(
                travelGroup.getId(), user.getId()))
                .thenReturn(false);

        when(userService.findById(user.getId())).thenReturn(user);

        LocalDateTime before = LocalDateTime.now();
        travelRequestService.createTravelRequest(travelGroup, user.getId(), message);

        verify(travelRequestRepository).save(captor.capture());
        TravelRequest result = captor.getValue();

        assertEquals(travelGroup, result.getTravelGroup());
        assertEquals(user, result.getApplicant());
        assertNull(result.getRespondedOn());
        assertEquals(TravelRequestStatus.PENDING, result.getStatus());
        assertEquals(message, result.getMessage());
        assertFalse(result.getRequestedOn().isBefore(before));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    public void createTravelRequest_shouldCreateRequestWithoutMessage_whenMessageIsNullOrBlank(
            String message) {

        TravelGroup travelGroup = TravelGroupFixture.create();
        User user = UserFixture.createUser();

        when(travelRequestRepository.countByTravelGroupIdAndStatus(
                travelGroup.getId(), TravelRequestStatus.APPROVED)).thenReturn(2);

        when(travelRequestRepository.existsByTravelGroupIdAndApplicantId(
                travelGroup.getId(), user.getId())).thenReturn(false);

        when(userService.findById(user.getId()))
                .thenReturn(user);

        travelRequestService.createTravelRequest(travelGroup, user.getId(), message);

        verify(travelRequestRepository).save(captor.capture());

        TravelRequest result = captor.getValue();

        assertNull(result.getMessage());
        assertEquals(TravelRequestStatus.PENDING, result.getStatus());
        assertNull(result.getRespondedOn());
    }

    @Test
    public void countAvailableSpots_shouldReturnValidNumber() {
        TravelGroup travelGroup = TravelGroupFixture.create();

        when(travelRequestRepository.countByTravelGroupIdAndStatus(
                travelGroup.getId(), TravelRequestStatus.APPROVED))
                .thenReturn(3);

        assertEquals(0, travelRequestService.countAvailableSpots(travelGroup));
    }

    @Test
    public void acceptTravelRequest_shouldThrowNoSuchElementException_whenInvalidTravelRequest() {
        UUID requestId = UUID.randomUUID();
        UUID travelGroupId = UUID.randomUUID();

        when(travelRequestRepository.findByIdAndTravelGroupId(requestId, travelGroupId))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class
                , () -> travelRequestService.acceptTravelRequest(
                        travelGroupId, requestId, UUID.randomUUID(), 2));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }

    @Test
    public void acceptTravelRequest_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        UUID travelGroupId = UUID.randomUUID();
        TravelRequest travelRequest = TravelRequestFixture.create();


        when(travelRequestRepository.findByIdAndTravelGroupId(travelRequest.getId(), travelGroupId))
                .thenReturn(Optional.of(travelRequest));

        assertThrows(AccessDeniedException.class
                , () -> travelRequestService.acceptTravelRequest(
                        travelGroupId, travelRequest.getId(), UUID.randomUUID(), 2));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }

    @ParameterizedTest
    @EnumSource(value = TravelGroupStatus.class, names = {"FULL", "CLOSED", "CANCELLED"})
    public void acceptTravelRequest_shouldThrowIllegalStateException_whenTravelGroupIsNotOpen(
            TravelGroupStatus status) {
        TravelGroup travelGroup = TravelGroupFixture.create();
        travelGroup.setStatus(status);
        TravelRequest travelRequest = TravelRequestFixture.create();
        travelRequest.setTravelGroup(travelGroup);
        UUID ownerId = travelRequest.getTravelGroup().getOwner().getId();

        when(travelRequestRepository.findByIdAndTravelGroupId(travelRequest.getId(), travelGroup.getId()))
                .thenReturn(Optional.of(travelRequest));

        assertThrows(IllegalStateException.class
                , () -> travelRequestService.acceptTravelRequest(
                        travelGroup.getId(), travelRequest.getId(), ownerId, 2));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }

    @ParameterizedTest
    @EnumSource(value = TravelRequestStatus.class, names = {"APPROVED", "REJECTED", "CANCELLED"})
    public void acceptTravelRequest_shouldThrowIllegalStateException_whenTravelRequestStatusIsNotPending(
            TravelRequestStatus status) {
        UUID travelGroupId = UUID.randomUUID();
        TravelRequest travelRequest = TravelRequestFixture.create();
        travelRequest.setStatus(status);
        UUID ownerId = travelRequest.getTravelGroup().getOwner().getId();

        when(travelRequestRepository.findByIdAndTravelGroupId(travelRequest.getId(), travelGroupId))
                .thenReturn(Optional.of(travelRequest));

        assertThrows(IllegalStateException.class
                , () -> travelRequestService.acceptTravelRequest(
                        travelGroupId, travelRequest.getId(), ownerId, 2));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }

    @Test
    public void acceptTravelRequest_shouldThrowTravelGroupFullException_whenAvailableSpotsIsNonPositive() {
        UUID travelGroupId = UUID.randomUUID();
        TravelRequest travelRequest = TravelRequestFixture.createWithPendingStatus();
        UUID ownerId = travelRequest.getTravelGroup().getOwner().getId();

        when(travelRequestRepository.findByIdAndTravelGroupId(travelRequest.getId(), travelGroupId))
                .thenReturn(Optional.of(travelRequest));

        assertThrows(TravelGroupFullException.class
                , () -> travelRequestService.acceptTravelRequest(
                        travelGroupId, travelRequest.getId(), ownerId, 0));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }

    @Test
    public void acceptTravelRequest_shouldAcceptRequest() {
        UUID travelGroupId = UUID.randomUUID();
        TravelRequest travelRequest = TravelRequestFixture.createWithPendingStatus();
        UUID ownerId = travelRequest.getTravelGroup().getOwner().getId();

        when(travelRequestRepository.findByIdAndTravelGroupId(travelRequest.getId(), travelGroupId))
                .thenReturn(Optional.of(travelRequest));

        travelRequestService.acceptTravelRequest(
                travelGroupId, travelRequest.getId(), ownerId, 2);

        verify(travelRequestRepository).save(captor.capture());
        TravelRequest result = captor.getValue();

        assertEquals(TravelRequestStatus.APPROVED, result.getStatus());
        assertNotNull(result.getRespondedOn());
    }

    @Test
    public void rejectTravelRequest_shouldThrowNoSuchElementException_whenInvalidTravelRequest() {
        UUID requestId = UUID.randomUUID();
        UUID travelGroupId = UUID.randomUUID();

        when(travelRequestRepository.findByIdAndTravelGroupId(requestId, travelGroupId))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class
                , () -> travelRequestService.rejectTravelRequest(
                        travelGroupId, requestId, UUID.randomUUID()));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }

    @Test
    public void rejectTravelRequest_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        UUID travelGroupId = UUID.randomUUID();
        TravelRequest request = TravelRequestFixture.create();

        when(travelRequestRepository.findByIdAndTravelGroupId(request.getId(), travelGroupId))
                .thenReturn(Optional.of(request));

        assertThrows(AccessDeniedException.class
                , () -> travelRequestService.rejectTravelRequest(
                        travelGroupId, request.getId(), UUID.randomUUID()));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));
    }


    @ParameterizedTest
    @EnumSource(value = TravelRequestStatus.class, names = {"APPROVED", "REJECTED", "CANCELLED"})
    public void rejectTravelRequest_shouldThrowAccessDeniedException_whenTravelStatusNotPending(
            TravelRequestStatus status) {
        UUID travelGroupId = UUID.randomUUID();
        TravelRequest request = TravelRequestFixture.createWithPendingStatus();
        request.setStatus(status);
        UUID ownerId = request.getTravelGroup().getOwner().getId();

        when(travelRequestRepository.findByIdAndTravelGroupId(request.getId(), travelGroupId))
                .thenReturn(Optional.of(request));

        assertThrows(AccessDeniedException.class
                , () -> travelRequestService.rejectTravelRequest(
                        travelGroupId, request.getId(), ownerId));

        verify(travelRequestRepository, never()).save(any(TravelRequest.class));

    }

    @Test
    public void rejectTravelRequest_shouldRejectTravelRequestSuccessfully() {
        UUID travelGroupId = UUID.randomUUID();
        TravelRequest request = TravelRequestFixture.createWithPendingStatus();
        UUID ownerId = request.getTravelGroup().getOwner().getId();

        when(travelRequestRepository.findByIdAndTravelGroupId(request.getId(), travelGroupId))
                .thenReturn(Optional.of(request));

        travelRequestService.rejectTravelRequest(travelGroupId, request.getId(), ownerId);

        verify(travelRequestRepository).save(captor.capture());

        TravelRequest result = captor.getValue();

        assertEquals(TravelRequestStatus.REJECTED, request.getStatus());
        assertNotNull(result.getRespondedOn());
    }

    @Test
    public void getAllPendingRequests_shouldReturnEmptyList() {
        UUID travelGroupId = UUID.randomUUID();

        List<TravelRequest> expected = new ArrayList<>();
        when(travelRequestRepository.findAllByTravelGroupIdAndStatusOrderByRequestedOnDesc(
                travelGroupId, TravelRequestStatus.APPROVED)).thenReturn(List.of());

        assertEquals(expected, travelRequestService.getAllPendingRequests(
                travelGroupId, TravelRequestStatus.APPROVED));
    }

    @Test
    public void getAllPendingRequests_shouldReturnListOfTravelRequests() {
        UUID travelGroupId = UUID.randomUUID();

        List<TravelRequest> expected = TravelRequestFixture.createList();
        when(travelRequestRepository.findAllByTravelGroupIdAndStatusOrderByRequestedOnDesc(
                travelGroupId, TravelRequestStatus.APPROVED)).thenReturn(expected);

        assertEquals(expected, travelRequestService.getAllPendingRequests(
                travelGroupId, TravelRequestStatus.APPROVED));
    }

    @Test
    public void hasRequestFromUser_shouldReturnTrue_whenTravelRequestExists(){
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(travelRequestRepository.existsByTravelGroupIdAndApplicantId(travelGroupId, userId))
                .thenReturn(true);
        assertTrue(travelRequestService.hasRequestFromUser(travelGroupId, userId));
    }

    @Test
    public void hasRequestFromUser_shouldReturnFalse_whenTravelRequestDoesNotExist(){
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(travelRequestRepository.existsByTravelGroupIdAndApplicantId(travelGroupId, userId))
                .thenReturn(false);
        assertFalse(travelRequestService.hasRequestFromUser(travelGroupId, userId));
    }

    @Test
    public void getApprovedApplicantsForTravelGroup_shouldReturnEmptyList(){
        UUID travelGroupId = UUID.randomUUID();
        List<TravelRequest> expected = new ArrayList<>();
        when(travelRequestRepository.findAllByTravelGroupIdAndStatusOrderByRespondedOnDesc(
                travelGroupId, TravelRequestStatus.APPROVED))
                .thenReturn(List.of());

        assertEquals(expected, travelRequestService.getApprovedApplicantsForTravelGroup(travelGroupId));
    }

    @Test
    public void getApprovedApplicantsForTravelGroup_shouldReturnListOfTravelRequestsWithApprovedStatus(){
        UUID travelGroupId = UUID.randomUUID();
        List<TravelRequest> expected = TravelRequestFixture.createList();
        when(travelRequestRepository.findAllByTravelGroupIdAndStatusOrderByRespondedOnDesc(
                travelGroupId, TravelRequestStatus.APPROVED))
                .thenReturn(expected);

        assertEquals(expected, travelRequestService.getApprovedApplicantsForTravelGroup(travelGroupId));
    }

    @Test
    public void isApprovedMember_shouldReturnTrue_whenTravelRequestIsApproved(){
        UUID travelGroupId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        when(travelRequestRepository.existsByTravelGroupIdAndApplicantIdAndStatus(
                travelGroupId,applicantId,TravelRequestStatus.APPROVED))
                .thenReturn(true);

        assertTrue(travelRequestService.isApprovedMember(travelGroupId, applicantId));
    }

    @Test
    public void isApprovedMember_shouldReturnFalse_whenTravelRequestIsNotApproved(){
        UUID travelGroupId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        when(travelRequestRepository.existsByTravelGroupIdAndApplicantIdAndStatus(
                travelGroupId,applicantId,TravelRequestStatus.APPROVED))
                .thenReturn(false);

        assertFalse(travelRequestService.isApprovedMember(travelGroupId, applicantId));
    }
}

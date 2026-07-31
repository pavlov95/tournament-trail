package tournament_trail.demo.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelGroupComment;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import tournament_trail.demo.exceptions.InvalidCommentException;
import tournament_trail.demo.exceptions.TravelGroupDoesNotExistException;
import tournament_trail.demo.fixtures.TravelGroupCommentFixture;
import tournament_trail.demo.fixtures.TravelGroupFixture;
import tournament_trail.demo.fixtures.TravelRequestFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.TravelGroupCommentRepository;
import tournament_trail.demo.web.dtos.TravelGroupCommentsPageData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TravelGroupCommentServiceTest {
    @Mock
    private TravelGroupCommentRepository travelGroupCommentRepository;

    @Mock
    private TravelGroupService travelGroupService;

    @Mock
    private TravelRequestService travelRequestService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TravelGroupCommentService travelGroupCommentService;

    @Captor
    private ArgumentCaptor<TravelGroupComment> captor;

    @BeforeEach
    public void setUp(){

    }

    @Test
    public void getCommentsForGroup_shouldThrowTravelGroupDoesNotExistException() {
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(travelGroupService.findById(travelGroupId)).thenThrow(TravelGroupDoesNotExistException.class);

        assertThrows(TravelGroupDoesNotExistException.class
                , () -> travelGroupCommentService.getCommentsForGroup(travelGroupId, userId));
        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void getCommentsForGroup_shouldThrowAccessDeniedExceptionWhenStatusIsCancelled() {
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TravelGroup travelGroup = TravelGroupFixture.createWithCancelledStatus(travelGroupId);
        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);

        assertThrows(AccessDeniedException.class
                , () -> travelGroupCommentService.getCommentsForGroup(travelGroupId, userId));
        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void getCommentsForGroup_shouldThrowAccessDeniedExceptionWhenUserIsNotInTravelGroup() {
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TravelGroup travelGroup = TravelGroupFixture.createWithIdOwnerStatusMaxMembers(
                travelGroupId, UUID.randomUUID(), TravelGroupStatus.OPEN, 4);

        List<TravelRequest> travelRequests = TravelRequestFixture.createList(2);

        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);

        when(travelRequestService.getApprovedApplicantsForTravelGroup(travelGroupId))
                .thenReturn(travelRequests);

        assertThrows(AccessDeniedException.class
                , () -> travelGroupCommentService.getCommentsForGroup(travelGroupId, userId));
        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void getCommentsForGroup_shouldReturnListOfTravelGroupComments() {
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TravelGroup travelGroup = TravelGroupFixture.createWithIdOwnerStatusMaxMembers(
                travelGroupId, userId, TravelGroupStatus.OPEN, 4);

        List<TravelRequest> travelRequests = TravelRequestFixture.createList(2);

        TravelGroupComment firstComment = TravelGroupComment.builder()
                .travelGroup(travelGroup)
                .build();
        TravelGroupComment secondComment = TravelGroupComment.builder()
                .travelGroup(travelGroup)
                .build();

        List<TravelGroupComment> expected = List.of(firstComment, secondComment);
        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);

        when(travelRequestService.getApprovedApplicantsForTravelGroup(travelGroupId)).thenReturn(travelRequests);

        when(travelGroupCommentRepository
                .findAllByTravelGroupIdAndHiddenFalseOrderByPinnedDescCreatedOnDesc(travelGroupId))
                .thenReturn(expected);
        assertEquals(expected, travelGroupCommentService.getCommentsForGroup(travelGroupId, userId));
    }

    @Test
    public void createComment_shouldCreateValidComment() {
        UUID travelGroupId = UUID.randomUUID();
        String content = " Test ";
        UUID userId = UUID.randomUUID();
        LocalDateTime before = LocalDateTime.now();

        TravelGroup travelGroup = TravelGroupFixture.create(travelGroupId, userId);
        User user = UserFixture.createUser(userId);
        List<TravelRequest> requests = TravelRequestFixture.createList(2);

        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);
        when(userService.findById(userId)).thenReturn(user);
        when(travelRequestService.getApprovedApplicantsForTravelGroup(travelGroupId)).thenReturn(requests);

        travelGroupCommentService.createComment(travelGroupId, content, userId);

        verify(travelGroupCommentRepository).save(captor.capture());

        TravelGroupComment result = captor.getValue();

        assertEquals("Test", result.getContent());
        assertEquals(user, result.getAuthor());
        assertFalse(result.isHidden());
        assertFalse(result.isPinned());
        assertNull(result.getEditedOn());
        assertEquals(travelGroup, result.getTravelGroup());
        assertTrue(before.isBefore(result.getCreatedOn()));
    }

    @Test
    public void editComment_shouldThrowTravelGroupDoesNotExistException() {
        UUID travelGroupId = UUID.randomUUID();
        when(travelGroupService.findById(travelGroupId))
                .thenThrow(TravelGroupDoesNotExistException.class);

        assertThrows(TravelGroupDoesNotExistException.class
                , () -> travelGroupCommentService.editComment(
                        travelGroupId, UUID.randomUUID(), UUID.randomUUID(), "test"));
        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void editComment_shouldEditComment() {
        UUID travelGroupId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        LocalDateTime before = LocalDateTime.now();
        TravelGroup travelGroup = TravelGroupFixture.createWithIdOwnerStatusMaxMembers(
                travelGroupId, ownerId, TravelGroupStatus.OPEN, 4);
        User user = UserFixture.createUser(ownerId);

        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);
        TravelGroupComment comment = TravelGroupCommentFixture.create(
                commentId, travelGroup, user, " Test");

        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(commentId, travelGroupId))
                .thenReturn(Optional.of(comment));
        travelGroupCommentService.editComment(travelGroupId, ownerId, commentId, " Test");

        verify(travelGroupCommentRepository).save(captor.capture());

        TravelGroupComment result = captor.getValue();
        assertEquals("Test", result.getContent());
        assertTrue(before.isBefore(result.getEditedOn()));
    }

    @Test
    public void editComment_shouldThrowInvalidCommentException() {
        UUID travelGroupId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        TravelGroup travelGroup = TravelGroupFixture.createWithIdOwnerStatusMaxMembers(
                travelGroupId, ownerId, TravelGroupStatus.OPEN, 4);

        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);
        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(commentId, travelGroupId))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCommentException.class
                , () -> travelGroupCommentService.editComment(travelGroupId, ownerId, commentId, " Test"));
        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void editComment_shouldEditThrowAccessDeniedExceptionBecauseUserEditsAnotherUserComment() {
        UUID travelGroupId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        TravelGroup travelGroup = TravelGroupFixture.createWithIdOwnerStatusMaxMembers(
                travelGroupId, ownerId, TravelGroupStatus.OPEN, 4);
        User user = UserFixture.createUser(UUID.randomUUID());

        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);
        TravelGroupComment comment = TravelGroupCommentFixture.create(
                commentId, travelGroup, user, " Test");

        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(commentId, travelGroupId))
                .thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class
                , () -> travelGroupCommentService.editComment(
                        travelGroupId, ownerId, commentId, " Test"));
        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void pinComment_shouldPinCommentCorrectly() {
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User user = UserFixture.createUser(userId);

        TravelGroup travelGroup = TravelGroupFixture.createWithIdOwnerStatusMaxMembers(
                travelGroupId, userId, TravelGroupStatus.OPEN, 4);
        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);

        TravelGroupComment comment = TravelGroupCommentFixture.create(
                commentId, travelGroup, user, "Test");
        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(commentId, travelGroupId))
                .thenReturn(Optional.of(comment));

        travelGroupCommentService.pinComment(travelGroupId, userId, commentId);

        verify(travelGroupCommentRepository).save(captor.capture());
        TravelGroupComment result = captor.getValue();

        assertTrue(result.isPinned());
    }

    @Test
    public void unpinComment_shouldUnpinCommentSuccessfully() {
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User user = UserFixture.createUser(userId);

        TravelGroup travelGroup = TravelGroupFixture.createWithIdOwnerStatusMaxMembers(
                travelGroupId, userId, TravelGroupStatus.OPEN, 4);
        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);

        TravelGroupComment comment = TravelGroupCommentFixture.create(
                commentId, travelGroup, user, "Test");
        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(commentId, travelGroupId))
                .thenReturn(Optional.of(comment));

        travelGroupCommentService.unpinComment(travelGroupId, userId, commentId);

        verify(travelGroupCommentRepository).save(captor.capture());
        TravelGroupComment result = captor.getValue();

        assertFalse(result.isPinned());
    }

    @Test
    public void deleteComment_shouldDeleteCommentSuccessfully() {
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User user = UserFixture.createUser(userId);

        TravelGroup travelGroup = TravelGroupFixture.createWithIdOwnerStatusMaxMembers(
                travelGroupId, userId, TravelGroupStatus.OPEN, 4);
        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);

        TravelGroupComment comment = TravelGroupCommentFixture.create(
                commentId, travelGroup, user, "Test");
        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(commentId, travelGroupId))
                .thenReturn(Optional.of(comment));

        travelGroupCommentService.deleteComment(travelGroupId, userId, commentId);

        verify(travelGroupCommentRepository).save(captor.capture());
        TravelGroupComment result = captor.getValue();

        assertTrue(result.isHidden());
    }

    @Test
    public void getCommentsPageData_shouldReturnTravelGroupCommentsPageDataSuccessfully() {
        UUID travelGroupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = UserFixture.createUser(userId);
        int countVisibleComments = 2;
        TravelGroup travelGroup = TravelGroupFixture.createWithIdOwnerStatusMaxMembers(
                travelGroupId, userId, TravelGroupStatus.OPEN, 4);

        List<TravelGroupComment> comments = TravelGroupCommentFixture.createList(
                2, travelGroup, user, "Test");

        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);
        when(travelGroupCommentRepository
                .findAllByTravelGroupIdAndHiddenFalseOrderByPinnedDescCreatedOnDesc(travelGroupId))
                .thenReturn(comments);
        when(travelGroupCommentRepository.countByTravelGroupIdAndHiddenFalse(travelGroupId))
                .thenReturn(2);
        TravelGroupCommentsPageData expected = new TravelGroupCommentsPageData(
                travelGroup, comments, countVisibleComments);
        TravelGroupCommentsPageData result = travelGroupCommentService.getCommentsPageData(travelGroupId, userId);
        assertEquals(expected.travelGroup(), result.travelGroup());
        assertEquals(expected.comments(), result.comments());
        assertEquals(expected.countVisibleComments(), result.countVisibleComments());

    }
}


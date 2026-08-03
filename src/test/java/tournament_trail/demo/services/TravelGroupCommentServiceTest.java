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
import org.springframework.security.access.AccessDeniedException;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelGroupComment;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.exceptions.InvalidCommentException;
import tournament_trail.demo.exceptions.TravelGroupDoesNotExistException;
import tournament_trail.demo.fixtures.TravelGroupCommentFixture;
import tournament_trail.demo.fixtures.TravelGroupFixture;
import tournament_trail.demo.fixtures.TravelRequestFixture;

import tournament_trail.demo.repositories.TravelGroupCommentRepository;
import tournament_trail.demo.web.dtos.TravelGroupCommentsPageData;

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

    @InjectMocks
    private TravelGroupCommentService travelGroupCommentService;

    @Captor
    private ArgumentCaptor<TravelGroupComment> captor;

     public enum AccessType {
        OWNER,
        APPROVED_APPLICANT
    }

    @Test
    public void getCommentsForGroup_shouldThrowTravelGroupDoesNotExistException_whenTravelGroupInvalid() {
        UUID travelGroupId = UUID.randomUUID();

        when(travelGroupService.findById(travelGroupId)).thenThrow(TravelGroupDoesNotExistException.class);

        assertThrows(TravelGroupDoesNotExistException.class
                , () -> travelGroupCommentService.getCommentsForGroup(travelGroupId, UUID.randomUUID()));

        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void getCommentsForGroup_shouldThrowAccessDeniedException_whenStatusIsCancelled() {
        TravelGroup travelGroup = TravelGroupFixture.createWithCancelledStatus();

        when(travelGroupService.findById(travelGroup.getId())).thenReturn(travelGroup);

        assertThrows(AccessDeniedException.class
                , () -> travelGroupCommentService.getCommentsForGroup(
                        travelGroup.getId(), UUID.randomUUID()));

        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void getCommentsForGroup_shouldThrowAccessDeniedException_whenUserIsNotInTravelGroup() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        List<TravelRequest> travelRequests = TravelRequestFixture.createList();

        when(travelGroupService.findById(travelGroup.getId())).thenReturn(travelGroup);

        when(travelRequestService.getApprovedApplicantsForTravelGroup(travelGroup.getId()))
                .thenReturn(travelRequests);

        assertThrows(AccessDeniedException.class
                , () -> travelGroupCommentService.getCommentsForGroup(
                        travelGroup.getId(), UUID.randomUUID()));

        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @ParameterizedTest
    @EnumSource(AccessType.class)
    public void getCommentsForGroup_shouldReturnListOfTravelGroupComments_whenUserHasAccess(
            AccessType accessType) {

        List<TravelRequest> travelRequests = TravelRequestFixture.createList();

        TravelGroup travelGroup = travelRequests.get(0).getTravelGroup();
        UUID travelGroupId = travelGroup.getId();

        UUID userId = switch (accessType) {
            case OWNER -> travelGroup.getOwner().getId();
            case APPROVED_APPLICANT -> travelRequests.get(0).getApplicant().getId();
        };

        List<TravelGroupComment> expected = TravelGroupCommentFixture.createList();
        expected.forEach(comment -> comment.setTravelGroup(travelGroup));

        when(travelGroupService.findById(travelGroupId))
                .thenReturn(travelGroup);

        when(travelRequestService.getApprovedApplicantsForTravelGroup(travelGroupId))
                .thenReturn(travelRequests);

        when(travelGroupCommentRepository
                .findAllByTravelGroupIdAndHiddenFalseOrderByPinnedDescCreatedOnDesc(travelGroupId))
                .thenReturn(expected);

        List<TravelGroupComment> result = travelGroupCommentService.getCommentsForGroup(
                travelGroupId, userId);

        assertEquals(expected, result);

        verify(travelGroupCommentRepository)
                .findAllByTravelGroupIdAndHiddenFalseOrderByPinnedDescCreatedOnDesc(travelGroupId);
    }

    @ParameterizedTest
    @EnumSource(AccessType.class)
    public void editComment_shouldEditComment_whenUserHasAccessAndIsAuthor(AccessType accessType) {
        List<TravelRequest> travelRequests = TravelRequestFixture.createList();

        TravelGroup travelGroup = travelRequests.get(0).getTravelGroup();
        UUID travelGroupId = travelGroup.getId();
        UUID commentId = UUID.randomUUID();

        User user = switch (accessType) {
            case OWNER -> travelGroup.getOwner();
            case APPROVED_APPLICANT -> travelRequests.get(0).getApplicant();
        };

        TravelGroupComment comment = TravelGroupCommentFixture.create();
        comment.setAuthor(user);

        when(travelGroupService.findById(travelGroupId)).thenReturn(travelGroup);

        when(travelRequestService.getApprovedApplicantsForTravelGroup(travelGroupId))
                .thenReturn(travelRequests);

        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(commentId, travelGroupId))
                .thenReturn(Optional.of(comment));

        travelGroupCommentService.editComment(
                travelGroupId, user.getId(), commentId, " Test edit content ");

        verify(travelGroupCommentRepository).save(captor.capture());
        TravelGroupComment result = captor.getValue();

        assertEquals("Test edit content", result.getContent());
        assertNotNull(result.getEditedOn());
    }

    @Test
    public void editComment_shouldThrowTravelGroupDoesNotExistException() {
        UUID travelGroupId = UUID.randomUUID();
        when(travelGroupService.findById(travelGroupId))
                .thenThrow(TravelGroupDoesNotExistException.class);

        assertThrows(TravelGroupDoesNotExistException.class
                , () -> travelGroupCommentService.editComment(
                        travelGroupId, UUID.randomUUID(), UUID.randomUUID(), "TEST_EDIT_CONTENT"));

        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void editComment_shouldThrowInvalidCommentException_whenThereIsNoTravelGroupComment() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        UUID commentId = UUID.randomUUID();
        when(travelGroupService.findById(travelGroup.getId())).thenReturn(travelGroup);
        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(
                commentId, travelGroup.getId()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCommentException.class
                , () -> travelGroupCommentService.editComment(
                        travelGroup.getId(), travelGroup.getOwner().getId(), commentId
                        , "TEST_EDIT_CONTENT"));

        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void editComment_shouldThrowAccessDeniedException_whenUserEditsAnotherUserComment() {
        UUID commentId = UUID.randomUUID();
        TravelGroup travelGroup = TravelGroupFixture.create();
        TravelGroupComment comment = TravelGroupCommentFixture.create();

        when(travelGroupService.findById(travelGroup.getId())).thenReturn(travelGroup);

        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(commentId, travelGroup.getId()))
                .thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class
                , () -> travelGroupCommentService.editComment(
                        travelGroup.getId(), travelGroup.getOwner().getId(), commentId
                        , "TEST_EDIT_CONTENT"));

        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void pinComment_shouldPinCommentCorrectly_whenUserIsOwnerOfTravelGroup() {
        TravelGroup travelGroup = TravelGroupFixture.create();

        when(travelGroupService.findById(travelGroup.getId())).thenReturn(travelGroup);

        TravelGroupComment comment = TravelGroupCommentFixture.create();

        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(
                comment.getId(), travelGroup.getId()))
                .thenReturn(Optional.of(comment));

        travelGroupCommentService.pinComment(
                travelGroup.getId(), travelGroup.getOwner().getId(), comment.getId());

        verify(travelGroupCommentRepository).save(captor.capture());
        TravelGroupComment result = captor.getValue();

        assertTrue(result.isPinned());
    }

    @Test
    public void unpinComment_shouldUnpinCommentSuccessfully_whenUserIsOwnerOfTravelGroup() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        TravelGroupComment comment = TravelGroupCommentFixture.create();

        when(travelGroupService.findById(travelGroup.getId())).thenReturn(travelGroup);

        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(
                comment.getId(), travelGroup.getId()))
                .thenReturn(Optional.of(comment));

        travelGroupCommentService.unpinComment(
                travelGroup.getId(), travelGroup.getOwner().getId(), comment.getId());

        verify(travelGroupCommentRepository).save(captor.capture());
        TravelGroupComment result = captor.getValue();

        assertFalse(result.isPinned());
    }

    @Test
    public void deleteComment_shouldDeleteCommentSuccessfully_whenUserIsAuthorOfComment() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        TravelGroupComment comment = TravelGroupCommentFixture.create();
        User author = comment.getAuthor();
        travelGroup.setOwner(author);

        when(travelGroupService.findById(travelGroup.getId())).thenReturn(travelGroup);

        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse
                (comment.getId(), travelGroup.getId()))
                .thenReturn(Optional.of(comment));

        travelGroupCommentService.deleteComment(
                travelGroup.getId(), author.getId(), comment.getId());

        verify(travelGroupCommentRepository).save(captor.capture());
        TravelGroupComment result = captor.getValue();

        assertTrue(result.isHidden());
    }

    @Test
    public void deleteComment_shouldThrowAccessDeniedException_whenUserIsNotAuthorOfComment() {
        TravelGroup travelGroup = TravelGroupFixture.create();
        TravelGroupComment comment = TravelGroupCommentFixture.create();

        when(travelGroupService.findById(travelGroup.getId())).thenReturn(travelGroup);

        when(travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(
                comment.getId(), travelGroup.getId()))
                .thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class,
                () -> travelGroupCommentService.deleteComment(
                        travelGroup.getId(), travelGroup.getOwner().getId(), comment.getId()));

        verify(travelGroupCommentRepository, never()).save(any(TravelGroupComment.class));
    }

    @Test
    public void getCommentsPageData_shouldReturnTravelGroupCommentsPageDataSuccessfully() {
        int countVisibleComments = 2;
        TravelGroup travelGroup = TravelGroupFixture.create();

        List<TravelGroupComment> comments = TravelGroupCommentFixture.createList();

        when(travelGroupService.findById(travelGroup.getId())).thenReturn(travelGroup);

        when(travelGroupCommentRepository
                .findAllByTravelGroupIdAndHiddenFalseOrderByPinnedDescCreatedOnDesc(travelGroup.getId()))
                .thenReturn(comments);

        when(travelGroupCommentRepository.countByTravelGroupIdAndHiddenFalse(travelGroup.getId()))
                .thenReturn(2);

        TravelGroupCommentsPageData expected = new TravelGroupCommentsPageData(
                travelGroup, comments, countVisibleComments);

        TravelGroupCommentsPageData result = travelGroupCommentService.getCommentsPageData(
                travelGroup.getId(), travelGroup.getOwner().getId());

        assertEquals(expected.travelGroup(), result.travelGroup());
        assertEquals(expected.comments(), result.comments());
        assertEquals(expected.countVisibleComments(), result.countVisibleComments());
    }
}


package tournament_trail.demo.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelGroupComment;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import tournament_trail.demo.exceptions.InvalidCommentContentException;
import tournament_trail.demo.exceptions.InvalidCommentException;
import tournament_trail.demo.repositories.TravelGroupCommentRepository;
import tournament_trail.demo.web.dtos.TravelGroupCommentsPageData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TravelGroupCommentService {
    private final TravelGroupCommentRepository travelGroupCommentRepository;
    private final TravelGroupService travelGroupService;
    private final TravelRequestService travelRequestService;
    private final UserService userService;

    public TravelGroupCommentService(TravelGroupCommentRepository travelGroupCommentRepository, TravelGroupService travelGroupService, TravelRequestService travelRequestService, UserService userService) {
        this.travelGroupCommentRepository = travelGroupCommentRepository;
        this.travelGroupService = travelGroupService;
        this.travelRequestService = travelRequestService;
        this.userService = userService;
    }

    public List<TravelGroupComment> getCommentsForGroup(UUID travelGroupId, UUID userId) {
        TravelGroup travelGroup = travelGroupService.findById(travelGroupId);
        checkForAccess(userId, travelGroup);
        return travelGroupCommentRepository.findAllByTravelGroupIdAndHiddenFalseOrderByPinnedDescCreatedOnDesc(travelGroupId);
    }

    @Transactional
    public void createComment(UUID travelGroupId, String content, UUID userId) {
        TravelGroup travelGroup = travelGroupService.findById(travelGroupId);
        checkForAccess(userId, travelGroup);

        if (content == null || content.isBlank() || content.length() > 1000) {
            throw new InvalidCommentContentException();
        }

        TravelGroupComment comment = TravelGroupComment.builder()
                .content(content.trim())
                .pinned(false)
                .author(userService.findById(userId))
                .travelGroup(travelGroup)
                .hidden(false)
                .createdOn(LocalDateTime.now())
                .editedOn(null)
                .build();
        travelGroupCommentRepository.save(comment);
    }

    @Transactional
    public void editComment(UUID travelGroupId, UUID userId, UUID commentId, String content) {
        TravelGroup travelGroup = travelGroupService.findById(travelGroupId);
        checkForAccess(userId, travelGroup);

        TravelGroupComment comment = findByIdAndTravelGroupId(commentId, travelGroupId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("You are only allowed to edit your own comments");
        }

        if (content == null || content.isBlank() || content.length() > 1000) {
            throw new InvalidCommentContentException();
        }

        comment.setContent(content.trim());
        comment.setEditedOn(LocalDateTime.now());
        travelGroupCommentRepository.save(comment);
    }



    @Transactional
    public void pinComment(UUID travelGroupId, UUID userId, UUID commentId) {
        TravelGroup travelGroup = travelGroupService.findById(travelGroupId);
        checkForAccess(userId, travelGroup);


        TravelGroupComment comment = findByIdAndTravelGroupId(commentId, travelGroupId);
        if (!travelGroup.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the owner of the travel group can pin comments.");
        }
        comment.setPinned(true);
        travelGroupCommentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(UUID travelGroupId, UUID userId, UUID commentId) {
        TravelGroup travelGroup = travelGroupService.findById(travelGroupId);
        checkForAccess(userId, travelGroup);

        TravelGroupComment comment = findByIdAndTravelGroupId(commentId, travelGroupId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("You are only allowed to delete your own comments");
        }

        comment.setHidden(true);
        travelGroupCommentRepository.save(comment);
    }

    @Transactional
    public void unpinComment(UUID travelGroupId, UUID userId, UUID commentId) {
        TravelGroup travelGroup = travelGroupService.findById(travelGroupId);
        checkForAccess(userId, travelGroup);

        TravelGroupComment comment = findByIdAndTravelGroupId(commentId, travelGroupId);

        if (!travelGroup.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the owner of the travel group can unpin comments.");
        }

        comment.setPinned(false);
        travelGroupCommentRepository.save(comment);
    }

    public int countVisibleComments(UUID travelGroupId) {
        return travelGroupCommentRepository.countByTravelGroupIdAndHiddenFalse(travelGroupId);
    }
    private TravelGroupComment findByIdAndTravelGroupId(UUID commentId, UUID travelGroupId) {
        return travelGroupCommentRepository.findByIdAndTravelGroupIdAndHiddenFalse(commentId, travelGroupId)
                .orElseThrow(InvalidCommentException::new);
    }

    private void checkForAccess(UUID currentUserId, TravelGroup travelGroup) {
        if (travelGroup.getStatus().equals(TravelGroupStatus.CANCELLED)) {
            throw new AccessDeniedException("You are not allowed to comment in an already cancelled travel group");
        }

        List<UUID> listUsersId = travelRequestService.getApprovedApplicantsForTravelGroup(travelGroup.getId())
                .stream()
                .map(x -> x.getApplicant().getId())
                .collect(Collectors.toList());

        listUsersId.add(travelGroup.getOwner().getId());
        for (UUID id : listUsersId) {
            if (id.equals(currentUserId)) {
                return;
            }
        }

        throw new AccessDeniedException("You are not part of this travel group");
    }

    public TravelGroupCommentsPageData getCommentsPageData(UUID travelGroupId, UUID userId) {
        TravelGroup travelGroup = travelGroupService.findById(travelGroupId);

        checkForAccess(userId, travelGroup);

        List<TravelGroupComment> comments = travelGroupCommentRepository
                .findAllByTravelGroupIdAndHiddenFalseOrderByPinnedDescCreatedOnDesc(travelGroupId);

        int countVisibleComments = travelGroupCommentRepository.countByTravelGroupIdAndHiddenFalse(travelGroupId);

        return new TravelGroupCommentsPageData(travelGroup, comments, countVisibleComments);
    }
}

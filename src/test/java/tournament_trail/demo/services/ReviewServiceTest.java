package tournament_trail.demo.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import tournament_trail.demo.entities.Review;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Rating;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.exceptions.InvalidReviewException;
import tournament_trail.demo.exceptions.ReviewAlreadyExistsException;
import tournament_trail.demo.exceptions.TournamentNotStartedException;
import tournament_trail.demo.fixtures.*;
import tournament_trail.demo.repositories.ReviewRepository;
import org.mockito.junit.jupiter.MockitoExtension;
import tournament_trail.demo.web.dtos.ReviewRequest;
import tournament_trail.demo.web.dtos.ReviewSummaryData;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TournamentService tournamentService;

    @Mock
    private TournamentRegistrationService tournamentRegistrationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReviewService reviewService;

    @Captor
    private ArgumentCaptor<Review> captor;

    @Test
    public void findAllByTournamentId_shouldReturnEmptyList() {
        UUID tournamentId = UUID.randomUUID();

        when(reviewRepository.findByTournamentIdOrderByCreatedOnDesc(tournamentId))
                .thenReturn(List.of());

        List<Review> result = reviewService.findAllByTournamentId(tournamentId);

        assertTrue(result.isEmpty());
        verify(reviewRepository).findByTournamentIdOrderByCreatedOnDesc(tournamentId);
    }

    @Test
    public void findAllByTournamentId_shouldReturnListWithReviews() {
        UUID tournamentId = UUID.randomUUID();
        List<Review> expected = List.of(Review.builder()
                        .id(UUID.randomUUID())
                        .build()
                , Review.builder().id(UUID.randomUUID()).build());
        when(reviewRepository.findByTournamentIdOrderByCreatedOnDesc(tournamentId)).thenReturn(expected);

        List<Review> result = reviewService.findAllByTournamentId(tournamentId);

        assertEquals(expected, result);
        verify(reviewRepository).findByTournamentIdOrderByCreatedOnDesc(tournamentId);
    }

    @Test
    public void createReview_shouldCreateValidReview() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID organiserId = UUID.randomUUID();

        User user = UserFixture.createUser(userId);
        Tournament tournament = TournamentFixture.createWithStatusStarted(tournamentId, organiserId);

        TournamentRegistration registration = TournamentRegistrationFixture.create();
        ReviewRequest reviewRequest = ReviewRequestFixture.createWithRandomData();

        when(tournamentService.findById(tournamentId)).thenReturn(tournament);

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournamentId, userId))
                .thenReturn(Optional.of(registration));

        when(reviewRepository.existsByTournamentIdAndAuthorId(tournamentId, userId)).thenReturn(false);

        when(userService.findById(userId)).thenReturn(user);

        reviewService.createReview(tournamentId, userId, reviewRequest);

        verify(reviewRepository).save(captor.capture());

        Review savedReview = captor.getValue();

        assertEquals("Great tournament", savedReview.getTitle());
        assertEquals("Very well organised event.", savedReview.getContent());
        assertEquals(Rating.GOOD, savedReview.getRating());
        assertEquals(user, savedReview.getAuthor());
        assertEquals(tournament, savedReview.getTournament());
        assertNotNull(savedReview.getCreatedOn());
        assertNotNull(savedReview.getUpdatedOn());
    }

    @Test
    public void createReview_shouldThrowTournamentNotStartedException() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewRequest reviewRequest = new ReviewRequest();

        Tournament tournament = TournamentFixture.createWithStatusRegistrationClosed();

        when(tournamentService.findById(tournamentId)).thenReturn(tournament);

        assertThrows(TournamentNotStartedException.class
                , () -> reviewService.createReview(tournamentId, userId, reviewRequest));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void createReview_shouldThrowAccessDeniedExceptionWhenTournamentRegistrationIsNull() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewRequest reviewRequest = new ReviewRequest();

        Tournament tournament = TournamentFixture.createWithStatusCompleted();

        when(tournamentService.findById(tournamentId)).thenReturn(tournament);

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournamentId, userId))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class
                , () -> reviewService.createReview(tournamentId, userId, reviewRequest));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void createReview_shouldThrowAccessDeniedExceptionWhenUserAndOrganiserAreSame() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID organiserId = userId;

        Tournament tournament =
                TournamentFixture.creteWithOrganiserIdAndStatusStarted(tournamentId, organiserId);

        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();

        when(tournamentService.findById(tournamentId)).thenReturn(tournament);

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournamentId, userId))
                .thenReturn(Optional.of(tournamentRegistration));

        assertThrows(AccessDeniedException.class
                , () -> reviewService.createReview(tournamentId, userId, new ReviewRequest()));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void createReview_shouldThrowReviewAlreadyExistsException() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID organiserId = UUID.randomUUID();

        Tournament tournament =
                TournamentFixture.creteWithOrganiserIdAndStatusStarted(tournamentId, organiserId);
        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();


        when(tournamentService.findById(tournamentId)).thenReturn(tournament);

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournamentId, userId))
                .thenReturn(Optional.of(tournamentRegistration));

        when(reviewRepository.existsByTournamentIdAndAuthorId(tournamentId, userId)).thenReturn(true);

        assertThrows(ReviewAlreadyExistsException.class
                , () -> reviewService.createReview(tournamentId, userId, new ReviewRequest()));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void editReview_shouldThrowInvalidReviewException() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID organiserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();
        Tournament tournament =
                TournamentFixture.creteWithOrganiserIdAndStatusStarted(tournamentId, organiserId);

        when(tournamentService.findById(tournamentId)).thenReturn(tournament);
        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournamentId, userId))
                .thenReturn(Optional.of(tournamentRegistration));

        when(reviewRepository.findByIdAndTournamentId(reviewId, tournamentId))
                .thenReturn(Optional.empty());

        assertThrows(InvalidReviewException.class
                , () -> reviewService.editReview(tournamentId, userId, reviewId, new ReviewRequest()));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void editReview_shouldThrowAccessDeniedException() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID organiserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();
        Tournament tournament =
                TournamentFixture.creteWithOrganiserIdAndStatusStarted(tournamentId, organiserId);

        Review review = ReviewFixture.createReviewWithUserId(reviewId, UUID.randomUUID());

        when(tournamentService.findById(tournamentId)).thenReturn(tournament);
        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournamentId, userId))
                .thenReturn(Optional.of(tournamentRegistration));

        when(reviewRepository.findByIdAndTournamentId(reviewId, tournamentId))
                .thenReturn(Optional.of(review));

        assertThrows(AccessDeniedException.class
                , () -> reviewService.editReview(tournamentId, userId, reviewId, new ReviewRequest()));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void editReview_shouldEditReview() {
        UUID tournamentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID organiserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();

        Tournament tournament =
                TournamentFixture.creteWithOrganiserIdAndStatusStarted(tournamentId, organiserId);
        Review review = ReviewFixture.createReviewWithUserId(reviewId, userId);

        ReviewRequest reviewRequest = ReviewRequestFixture.createWithRandomData();

        when(tournamentService.findById(tournamentId)).thenReturn(tournament);
        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournamentId, userId))
                .thenReturn(Optional.of(tournamentRegistration));

        when(reviewRepository.findByIdAndTournamentId(reviewId, tournamentId))
                .thenReturn(Optional.of(review));

        reviewService.editReview(tournamentId, userId, reviewId, reviewRequest);

        verify(reviewRepository).save(captor.capture());
        Review savedReview = captor.getValue();
        assertEquals(reviewRequest.getTitle(), savedReview.getTitle());
        assertEquals(reviewRequest.getContent(), savedReview.getContent());
        assertEquals(reviewRequest.getRating(), savedReview.getRating());
        assertNotNull(savedReview.getUpdatedOn());
    }

    @Test
    public void getReviewSummary_shouldReturnReviewSummaryDataWithEmptyList() {
        UUID tournamentId = UUID.randomUUID();
        double expectedAverageRating = 0.0;
        int expectedReviews = 0;

        when(reviewRepository.findByTournamentIdOrderByCreatedOnDesc(tournamentId))
                .thenReturn(List.of());
        ReviewSummaryData result = reviewService.getReviewSummary(tournamentId);
        assertEquals(expectedAverageRating, result.averageRating());
        assertEquals(expectedReviews, result.reviewCount());
    }

    @Test
    public void getReviewSummary_shouldReturnReviewSummaryDataWithManyReviews() {
        UUID tournamentId = UUID.randomUUID();
        List<Review> reviews = ReviewFixture.createListReviews(3);
        when(reviewRepository.findByTournamentIdOrderByCreatedOnDesc(tournamentId)).thenReturn(reviews);

        ReviewSummaryData result = reviewService.getReviewSummary(tournamentId);
        assertEquals(3.0, result.averageRating());
        assertEquals(3, result.reviewCount());
    }

    @Test
    public void delete_shouldThrowAccessDeniedExceptionWhenUserIsNotSameAndIsNotAdmin() {
        UUID tournamentId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Review review = ReviewFixture.createReviewWithUserId(reviewId, UUID.randomUUID());

        when(reviewRepository.findByIdAndTournamentId(reviewId, tournamentId))
                .thenReturn(Optional.of(review));
        assertThrows(AccessDeniedException.class
                , () -> reviewService.delete(tournamentId, reviewId, userId, Role.PLAYER));

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    public void delete_shouldDeleteReviewWhenUserIsAuthor() {
        UUID tournamentId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Review review = ReviewFixture.createReviewWithUserId(reviewId, userId);

        when(reviewRepository.findByIdAndTournamentId(reviewId, tournamentId))
                .thenReturn(Optional.of(review));

        reviewService.delete(tournamentId, reviewId, userId, Role.PLAYER);

        verify(reviewRepository).delete(review);
    }

    @Test
    public void delete_shouldDeleteReviewWhenUserIsAdmin() {
        UUID tournamentId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        Review review = ReviewFixture.createReviewWithUserId(reviewId, authorId);

        when(reviewRepository.findByIdAndTournamentId(reviewId, tournamentId))
                .thenReturn(Optional.of(review));

        reviewService.delete(tournamentId, reviewId, userId, Role.ADMIN);

        verify(reviewRepository).delete(review);
    }
}


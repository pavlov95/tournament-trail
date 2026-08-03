package tournament_trail.demo.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import tournament_trail.demo.entities.Review;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.exceptions.InvalidReviewException;
import tournament_trail.demo.exceptions.ReviewAlreadyExistsException;
import tournament_trail.demo.exceptions.TournamentNotStartedException;
import tournament_trail.demo.fixtures.*;
import tournament_trail.demo.repositories.ReviewRepository;
import org.mockito.junit.jupiter.MockitoExtension;
import tournament_trail.demo.web.dtos.ReviewRequest;
import tournament_trail.demo.web.dtos.ReviewSummaryData;

import java.time.LocalDateTime;
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
    public void findAllByTournamentId_shouldReturnEmptyList_whenThereAreNoReviews() {
        UUID tournamentId = UUID.randomUUID();

        when(reviewRepository.findByTournamentIdOrderByCreatedOnDesc(tournamentId))
                .thenReturn(List.of());

        List<Review> result = reviewService.findAllByTournamentId(tournamentId);

        assertTrue(result.isEmpty());
        verify(reviewRepository).findByTournamentIdOrderByCreatedOnDesc(tournamentId);
    }

    @Test
    public void findAllByTournamentId_shouldReturnListWithReviews() {
        List<Review> expected = ReviewFixture.createReviewsWithSameTournament();
        UUID tournamentId = expected.get(0).getTournament().getId();

        when(reviewRepository.findByTournamentIdOrderByCreatedOnDesc(tournamentId)).thenReturn(expected);

        List<Review> result = reviewService.findAllByTournamentId(tournamentId);

        assertEquals(expected, result);
        verify(reviewRepository).findByTournamentIdOrderByCreatedOnDesc(tournamentId);
    }

    @Test
    public void createReview_shouldCreateValidReview() {
        User user = UserFixture.createUser();
        Tournament tournament = TournamentFixture.createWithStatusStarted();
        TournamentRegistration registration = TournamentRegistrationFixture.create();
        ReviewRequest reviewRequest = ReviewRequestFixture.create();

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournament.getId(), user.getId()))
                .thenReturn(Optional.of(registration));

        when(reviewRepository.existsByTournamentIdAndAuthorId(tournament.getId(), user.getId())).thenReturn(false);

        when(userService.findById(user.getId())).thenReturn(user);

        reviewService.createReview(tournament.getId(), user.getId(), reviewRequest);

        verify(reviewRepository).save(captor.capture());
        Review result = captor.getValue();

        assertEquals(ReviewRequestFixture.TEST_TITLE, result.getTitle());
        assertEquals(ReviewRequestFixture.TEST_CONTENT, result.getContent());
        assertEquals(ReviewRequestFixture.TEST_RATING, result.getRating());
        assertEquals(user, result.getAuthor());
        assertEquals(tournament, result.getTournament());
        assertNotNull(result.getCreatedOn());
        assertNotNull(result.getUpdatedOn());
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, names =
            {"DRAFT", "PUBLISHED", "REGISTRATION_CLOSED", "CANCELLED"})
    public void createReview_shouldThrowTournamentNotStartedException_whenTournamentIsNotStartedOrComplete(
            TournamentStatus status) {

        ReviewRequest reviewRequest = new ReviewRequest();

        Tournament tournament = TournamentFixture.createWithStatus(status);

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        assertThrows(TournamentNotStartedException.class
                , () -> reviewService.createReview(tournament.getId(), UUID.randomUUID(), reviewRequest));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, names = {"STARTED", "COMPLETED"})
    public void createReview_shouldThrowAccessDeniedException_whenTournamentRegistrationIsNull(
            TournamentStatus status) {

        Tournament tournament = TournamentFixture.createWithStatus(status);
        ReviewRequest reviewRequest = new ReviewRequest();
        UUID userId = UUID.randomUUID();

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournament.getId(), userId))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class
                , () -> reviewService.createReview(tournament.getId(), userId, reviewRequest));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @ParameterizedTest
    @EnumSource(value = TournamentStatus.class, names = {"STARTED", "COMPLETED"})
    public void createReview_shouldThrowAccessDeniedException_whenUserAndOrganiserAreSame(
            TournamentStatus status) {

        Tournament tournament = TournamentFixture.createWithStatus(status);
        UUID organiserId = tournament.getOrganiser().getId();

        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournament.getId(), organiserId))
                .thenReturn(Optional.of(tournamentRegistration));

        assertThrows(AccessDeniedException.class
                , () -> reviewService.createReview(tournament.getId(), organiserId, new ReviewRequest()));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void createReview_shouldThrowReviewAlreadyExistsException() {
        Tournament tournament = TournamentFixture.createWithStatusStarted();
        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();
        UUID userId = UUID.randomUUID();

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournament.getId(), userId))
                .thenReturn(Optional.of(tournamentRegistration));

        when(reviewRepository.existsByTournamentIdAndAuthorId(tournament.getId(), userId)).thenReturn(true);

        assertThrows(ReviewAlreadyExistsException.class
                , () -> reviewService.createReview(tournament.getId(), userId, new ReviewRequest()));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void editReview_shouldThrowInvalidReviewException_whenThereIsNoReview() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();
        Tournament tournament = TournamentFixture.createWithStatusStarted();

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);
        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournament.getId(), userId))
                .thenReturn(Optional.of(tournamentRegistration));

        when(reviewRepository.findByIdAndTournamentId(reviewId, tournament.getId()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidReviewException.class
                , () -> reviewService.editReview(tournament.getId(), userId, reviewId, new ReviewRequest()));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void editReview_shouldThrowAccessDeniedException_whenUserIsNotReviewAuthor() {
        UUID userId = UUID.randomUUID();
        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();
        Tournament tournament = TournamentFixture.createWithStatusStarted();
        Review review = ReviewFixture.create();

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);
        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(tournament.getId(), userId))
                .thenReturn(Optional.of(tournamentRegistration));

        when(reviewRepository.findByIdAndTournamentId(review.getId(), tournament.getId()))
                .thenReturn(Optional.of(review));

        assertThrows(AccessDeniedException.class
                , () -> reviewService.editReview(
                        tournament.getId(), userId, review.getId(), new ReviewRequest()));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void editReview_shouldEditReview() {
        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create();
        Tournament tournament = TournamentFixture.createWithStatusStarted();
        Review review = ReviewFixture.create();

        ReviewRequest reviewRequest = ReviewRequestFixture.create();
        LocalDateTime before = LocalDateTime.now();

        when(tournamentService.findById(tournament.getId())).thenReturn(tournament);

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(
                tournament.getId(), review.getAuthor().getId()))
                .thenReturn(Optional.of(tournamentRegistration));

        when(reviewRepository.findByIdAndTournamentId(review.getId(), tournament.getId()))
                .thenReturn(Optional.of(review));

        reviewService.editReview(
                tournament.getId(), review.getAuthor().getId(), review.getId(), reviewRequest);

        verify(reviewRepository).save(captor.capture());
        Review result = captor.getValue();

        assertEquals(ReviewRequestFixture.TEST_TITLE, result.getTitle());
        assertEquals(ReviewRequestFixture.TEST_CONTENT, result.getContent());
        assertEquals(ReviewRequestFixture.TEST_RATING, result.getRating());
        assertNotNull(result.getUpdatedOn());
        assertTrue(before.isBefore(result.getUpdatedOn()));
    }

    @Test
    public void getReviewSummary_shouldReturnReviewSummaryData_whenThereAreNoReviews() {
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
    public void getReviewSummary_shouldReturnReviewSummaryData_whenThereAreReviews() {
        UUID tournamentId = UUID.randomUUID();
        List<Review> reviews = ReviewFixture.createListReviews(3);
        when(reviewRepository.findByTournamentIdOrderByCreatedOnDesc(tournamentId)).thenReturn(reviews);

        ReviewSummaryData result = reviewService.getReviewSummary(tournamentId);
        assertEquals(3.0, result.averageRating());
        assertEquals(3, result.reviewCount());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"PLAYER", "ORGANISER"})
    public void delete_shouldThrowAccessDeniedException_whenUserIsNotSameAndIsNotAdmin(Role role) {
        UUID tournamentId = UUID.randomUUID();
        Review review = ReviewFixture.create();

        when(reviewRepository.findByIdAndTournamentId(review.getId(), tournamentId))
                .thenReturn(Optional.of(review));
        assertThrows(AccessDeniedException.class
                , () -> reviewService.delete(tournamentId, review.getId(), UUID.randomUUID(), role));

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"PLAYER", "ORGANISER"})
    public void delete_shouldDeleteReview_whenUserIsAuthor(Role role) {
        UUID tournamentId = UUID.randomUUID();
        Review review = ReviewFixture.create();

        when(reviewRepository.findByIdAndTournamentId(review.getId(), tournamentId))
                .thenReturn(Optional.of(review));

        reviewService.delete(tournamentId, review.getId(), review.getAuthor().getId(), role);

        verify(reviewRepository).delete(review);
    }

    @Test
    public void delete_shouldDeleteReview_whenUserIsAdmin() {
        UUID tournamentId = UUID.randomUUID();
        Review review = ReviewFixture.create();

        when(reviewRepository.findByIdAndTournamentId(review.getId(), tournamentId))
                .thenReturn(Optional.of(review));

        reviewService.delete(tournamentId, review.getId(), UUID.randomUUID(), Role.ADMIN);

        verify(reviewRepository).delete(review);
    }
}


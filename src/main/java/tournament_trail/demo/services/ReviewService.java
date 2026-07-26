package tournament_trail.demo.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.Review;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.exceptions.InvalidReviewException;
import tournament_trail.demo.exceptions.TournamentNotStartedException;
import tournament_trail.demo.repositories.ReviewRepository;
import tournament_trail.demo.web.dtos.ReviewRequest;
import tournament_trail.demo.web.dtos.ReviewSummaryData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TournamentService tournamentService;
    private final TournamentRegistrationService tournamentRegistrationService;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository, TournamentService tournamentService, TournamentRegistrationService tournamentRegistrationService, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.tournamentService = tournamentService;
        this.tournamentRegistrationService = tournamentRegistrationService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Review> findAllByTournamentId(UUID tournamentId) {
        return reviewRepository.findByTournamentIdOrderByCreatedOnDesc(tournamentId);
    }

    @Transactional
    public void createReview(UUID tournamentId, UUID userId, ReviewRequest reviewRequest) {
        Tournament tournament = validateUserCanReviewTournament(tournamentId, userId);

        if (reviewRepository.existsByTournamentIdAndAuthorId(tournamentId, userId)) {
            throw new InvalidReviewException("You have already reviewed this tournament");
        }

        Review review = Review.builder()
                .content(reviewRequest.getContent())
                .rating(reviewRequest.getRating())
                .title(reviewRequest.getTitle())
                .author(userService.findById(userId))
                .tournament(tournament)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
    }

    @Transactional
    public void editReview(UUID tournamentId, UUID userId, UUID reviewId, ReviewRequest reviewRequest) {
        validateUserCanReviewTournament(tournamentId, userId);

        Review review = findByIdAndTournamentId(reviewId, tournamentId);

        if (!review.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("You can only alter your own reviews");
        }
        review.setTitle(reviewRequest.getTitle());
        review.setRating(reviewRequest.getRating());
        review.setContent(reviewRequest.getContent());
        review.setUpdatedOn(LocalDateTime.now());
        reviewRepository.save(review);
    }

    @Transactional
    public void delete(UUID tournamentId, UUID reviewId, UUID userId, Role role) {
        Review review = findByIdAndTournamentId(reviewId, tournamentId);
        boolean isAuthor = review.getAuthor().getId().equals(userId);
        boolean isAdmin = role == Role.ADMIN;

        if (!isAdmin && !isAuthor) {
            throw new AccessDeniedException("You can only delete your own Review");
        }
        reviewRepository.delete(review);
    }

    private Review findByIdAndTournamentId(UUID reviewId, UUID tournamentId) {
        return reviewRepository.findByIdAndTournamentId(reviewId, tournamentId).orElseThrow(
                () -> new InvalidReviewException("No such review exists"));
    }

    private Tournament validateUserCanReviewTournament(UUID tournamentId, UUID userId) {
        Tournament tournament = tournamentService.findById(tournamentId);
        TournamentStatus status = tournament.getStatus();
        if (status != TournamentStatus.COMPLETED && status != TournamentStatus.STARTED ) {
            throw new TournamentNotStartedException();
        }
        Optional<TournamentRegistration> optionalTournament = tournamentRegistrationService
                .findByTournamentIdAndPlayerId(tournamentId, userId);
        if (optionalTournament.isEmpty()) {
            throw new InvalidReviewException("You can not rate a tournament in which you do not have a registration");
        }
        if (tournament.getOrganiser().getId().equals(userId)) {
            throw new InvalidReviewException("An organiser can not rate his own tournament");
        }
        return tournament;
    }

    @Transactional(readOnly = true)
    public ReviewSummaryData getReviewSummary(UUID tournamentId) {
        List<Review> reviews = reviewRepository.findByTournamentIdOrderByCreatedOnDesc(tournamentId);

        double averageRating = reviews.stream()
                .mapToInt(review -> review.getRating().getGrade())
                .average()
                .orElse(0.0);

        return new ReviewSummaryData(reviews.size(), averageRating);
    }
}

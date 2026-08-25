package tournament_trail.demo.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tournament_trail.demo.entities.Review;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.fixtures.AuthenticationUserDetailsFixture;
import tournament_trail.demo.fixtures.ReviewFixture;
import tournament_trail.demo.fixtures.TournamentFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.fixtures.dtos.ReviewRequestFixture;
import tournament_trail.demo.repositories.*;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.TournamentRegistrationService;
import tournament_trail.demo.web.dtos.ReviewRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TravelGroupRepository travelGroupRepository;

    @Autowired
    private TravelGroupCommentRepository travelGroupCommentRepository;

    @Autowired
    private TravelRequestRepository travelRequestRepository;

    @Autowired
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    @MockitoBean
    private TournamentRegistrationService tournamentRegistrationService;

    private Tournament tournament;
    private AuthenticationUserDetails userDetails;
    private Review review;

    @BeforeEach
    public void setUp() {
        travelGroupCommentRepository.deleteAllInBatch();
        travelRequestRepository.deleteAllInBatch();
        tournamentRegistrationRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        travelGroupRepository.deleteAllInBatch();
        tournamentRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        User organiser = UserFixture.createWithAllFields();
        User author = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        tournament = TournamentFixture.createWithoutIdAndOrganiser(organiser);
        review = ReviewFixture.createWithoutUserAndTournament(author, tournament);

        userRepository.save(organiser);
        userRepository.save(author);
        userDetails = AuthenticationUserDetailsFixture.createFromUser(author);
        tournamentRepository.save(tournament);
        reviewRepository.save(review);

    }

    @Test
    public void getReviewsPage_whenAuthenticated_shouldReturnReviews() throws Exception {
        mockMvc.perform(get("/tournaments/{tournamentId}/reviews", tournament.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews"));
    }

    @Test
    public void getReviewsPage_whenNotAuthenticated_shouldRedirect() throws Exception {
        mockMvc.perform(get("/tournaments/{tournamentId}/reviews", tournament.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    public void editReview_whenValidData_shouldEditReview() throws Exception {
        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(
                tournament.getId(), userDetails.getId()))
                .thenReturn(Optional.of(new TournamentRegistration()));

        ReviewRequest reviewRequest = ReviewRequestFixture.create();
        mockMvc.perform(put("/tournaments/{tournamentId}/reviews/{reviewId}"
                        , tournament.getId(), review.getId())
                        .with(user(userDetails))
                        .with(csrf())
                        .param("title", reviewRequest.getTitle())
                        .param("content", reviewRequest.getContent())
                        .param("rating", reviewRequest.getRating().name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tournaments/" + tournament.getId() + "/reviews"));

        assertEquals(1, reviewRepository.count());

        Review result = reviewRepository.findById(review.getId()).orElseThrow();

        assertEquals(ReviewRequestFixture.TEST_UPDATED_TITLE, result.getTitle());
        assertEquals(ReviewRequestFixture.TEST_UPDATED_CONTENT, result.getContent());
    }

    @Test
    public void editReview_whenInvalidData_shouldNotEditReviewAndReturnReviewsView() throws Exception {
        ReviewRequest reviewRequest = ReviewRequestFixture.createInvalid();
        mockMvc.perform(put("/tournaments/{tournamentId}/reviews/{reviewId}"
                        , tournament.getId(), review.getId())
                        .with(user(userDetails))
                        .with(csrf())
                        .param("title", reviewRequest.getTitle())
                        .param("content", reviewRequest.getContent())
                        .param("rating", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews"));

        assertEquals(1, reviewRepository.count());

        Review result = reviewRepository.findById(review.getId()).orElseThrow();

        assertEquals(review.getTitle(), result.getTitle());
        assertEquals(review.getContent(), result.getContent());
    }

    @Test
    public void deleteReview_whenAuthenticated_shouldDeleteReview() throws Exception {
        mockMvc.perform(delete("/tournaments/{tournamentId}/reviews/{reviewId}"
                        , tournament.getId(), review.getId())
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tournaments/" + tournament.getId() + "/reviews"));

        assertEquals(0, reviewRepository.count());
    }

    @Test
    public void postReview_whenValidData_shouldCreateReview() throws Exception {
        User user = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(user);
        AuthenticationUserDetails userDetails = AuthenticationUserDetailsFixture.createFromUser(user);

        ReviewRequest reviewRequest = ReviewRequestFixture.create();

        when(tournamentRegistrationService.findByTournamentIdAndPlayerId(
                tournament.getId(), userDetails.getId()))
                .thenReturn(Optional.of(new TournamentRegistration()));

        mockMvc.perform(post("/tournaments/{tournamentId}/reviews", tournament.getId())
                        .with(user(userDetails))
                        .with(csrf())
                        .param("content", reviewRequest.getContent())
                        .param("title", reviewRequest.getTitle())
                        .param("rating", reviewRequest.getRating().name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tournaments/" + tournament.getId() + "/reviews"));

        assertEquals(2, reviewRepository.count());

        Review createdReview = reviewRepository.findAll()
                .stream()
                .filter(r -> r.getAuthor().getId().equals(userDetails.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(reviewRequest.getTitle(), createdReview.getTitle());
        assertEquals(reviewRequest.getContent(), createdReview.getContent());
        assertEquals(reviewRequest.getRating(), createdReview.getRating());
    }

    @Test
    public void postReview_whenInvalidData_shouldReturnReviewsViewAndNotCreateReview() throws Exception {
        mockMvc.perform(post("/tournaments/{tournamentId}/reviews", tournament.getId())
                        .with(user(userDetails))
                        .with(csrf())
                        .param("title", "")
                        .param("content", "")
                        .param("rating", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews"))
                .andExpect(model().attributeHasFieldErrors("reviewRequest"));

        assertEquals(1, reviewRepository.count());
    }
}

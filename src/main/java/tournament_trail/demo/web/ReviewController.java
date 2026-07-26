package tournament_trail.demo.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import tournament_trail.demo.entities.Review;
import tournament_trail.demo.entities.enums.Rating;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.ReviewService;
import tournament_trail.demo.web.dtos.ReviewRequest;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tournaments/{tournamentId}/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping()
    public ModelAndView getReviewsPage(@PathVariable UUID tournamentId) {
        ModelAndView modelAndView = new ModelAndView("reviews");
        modelAndView.addObject("reviewRequest", new ReviewRequest());
        addCommonData(modelAndView, tournamentId);

        return modelAndView;
    }

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ModelAndView postReview(@PathVariable UUID tournamentId, @Valid @ModelAttribute ReviewRequest reviewRequest
            , BindingResult bindingResult, @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("reviews");
            modelAndView.addObject("reviewRequest", reviewRequest);
            addCommonData(modelAndView, tournamentId);

            return modelAndView;
        }

        reviewService.createReview(tournamentId, userDetails.getId(), reviewRequest);
        return new ModelAndView("redirect:/tournaments/" + tournamentId + "/reviews");
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView editReview(@PathVariable UUID tournamentId, @PathVariable UUID reviewId
            , @Valid @ModelAttribute ReviewRequest reviewRequest, BindingResult bindingResult
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("reviews");
            addCommonData(modelAndView, tournamentId);
            modelAndView.addObject("reviewRequest", reviewRequest);

            return modelAndView;
        }

        reviewService.editReview(tournamentId, userDetails.getId(), reviewId, reviewRequest);

        return new ModelAndView("redirect:/tournaments/" + tournamentId + "/reviews");
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView deleteReview(@PathVariable UUID tournamentId, @PathVariable UUID reviewId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        reviewService.delete(tournamentId, reviewId, userDetails.getId(), userDetails.getRole());

        return new ModelAndView("redirect:/tournaments/" + tournamentId + "/reviews");
    }

    private void addCommonData(ModelAndView modelAndView, UUID tournamentId){
        List<Review> reviews = reviewService.findAllByTournamentId(tournamentId);
        modelAndView.addObject("tournamentId", tournamentId);
        modelAndView.addObject("reviews", reviews);
        modelAndView.addObject("ratings", Rating.values());
    }
}

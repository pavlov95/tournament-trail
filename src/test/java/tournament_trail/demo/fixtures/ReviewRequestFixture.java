package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.enums.Rating;
import tournament_trail.demo.web.dtos.ReviewRequest;

public class ReviewRequestFixture {
    public static final String TEST_CONTENT = "Test content";
    public static final String TEST_TITLE = "Great tournament";
    public static final Rating TEST_RATING = Rating.GOOD;

    public static ReviewRequest create(){
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setRating(TEST_RATING);
        reviewRequest.setContent(TEST_CONTENT);
        reviewRequest.setTitle(TEST_TITLE);
        return reviewRequest;
    }

}

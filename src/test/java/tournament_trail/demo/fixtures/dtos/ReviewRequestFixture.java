package tournament_trail.demo.fixtures.dtos;

import tournament_trail.demo.entities.enums.Rating;
import tournament_trail.demo.web.dtos.ReviewRequest;

public class ReviewRequestFixture {
    public static final String TEST_TITLE ="Test title";
    public static final String TEST_CONTENT ="Test content";

    public static final String TEST_UPDATED_TITLE = "Test updated title";
    public static final String TEST_UPDATED_CONTENT = "Test updated content";

    public static ReviewRequest createInvalid() {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setTitle("");
        reviewRequest.setContent("");
        return reviewRequest;
    }

    public static ReviewRequest create(){
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setRating(Rating.GOOD);
        reviewRequest.setTitle(TEST_UPDATED_TITLE);
        reviewRequest.setContent(TEST_UPDATED_CONTENT);
        return reviewRequest;
    }
}

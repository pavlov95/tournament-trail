package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.enums.Rating;
import tournament_trail.demo.web.dtos.ReviewRequest;

public class ReviewRequestFixture {
    public static ReviewRequest createWithRandomData(){
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setRating(Rating.GOOD);
        reviewRequest.setContent("Very well organised event.");
        reviewRequest.setTitle("Great tournament");
        return reviewRequest;
    }

}

package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.Review;
import tournament_trail.demo.entities.enums.Rating;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReviewFixture {
    public static Review createReview(UUID reviewId){
        return Review.builder()
                .id(reviewId)
                .tournament(TournamentFixture.create())
                .build();
    }

    public static Review createReviewWithUserId(UUID reviewId, UUID userId){
        return Review.builder()
                .id(reviewId)
                .author(UserFixture.createUser(userId))
                .build();
    }
    public static Review createWithNeutralRating(){
        return Review.builder().rating(Rating.NEUTRAL).build();
    }

    public static List<Review> createListReviews(int listSize){
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            reviews.add(ReviewFixture.createWithNeutralRating());
        }
        return reviews;
    }
}

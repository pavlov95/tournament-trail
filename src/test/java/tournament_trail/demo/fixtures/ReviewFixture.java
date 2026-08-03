package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.Review;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.enums.Rating;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReviewFixture {
    public static Review create(){
        return Review.builder()
                .id(UUID.randomUUID())
                .tournament(TournamentFixture.create())
                .author(UserFixture.createUser())
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

    public static List<Review> createReviewsWithSameTournament(){
        Review first = ReviewFixture.create();
        Tournament tournament = first.getTournament();

        Review second = ReviewFixture.create();
        second.setTournament(tournament);

        return List.of(first, second);
    }

}

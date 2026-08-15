package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelGroupComment;
import tournament_trail.demo.entities.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TravelGroupCommentFixture {
    public static final String TEST_CONTENT = "TEST_CONTENT";

    public static TravelGroupComment create() {
        return TravelGroupComment.builder()
                .id(UUID.randomUUID())
                .travelGroup(TravelGroupFixture.create())
                .hidden(false)
                .author(UserFixture.createUser())
                .content(TEST_CONTENT)
                .build();
    }

    public static List<TravelGroupComment> createList() {
        TravelGroupComment first = TravelGroupCommentFixture.create();
        TravelGroupComment second = TravelGroupCommentFixture.create();

        return List.of(first, second);
    }

    public static TravelGroupComment createWithAuthorAndTravelGroup(User author, TravelGroup travelGroup){
        return TravelGroupComment.builder()
                .travelGroup(travelGroup)
                .hidden(false)
                .author(author)
                .content(TEST_CONTENT)
                .createdOn(LocalDateTime.now())
                .editedOn(null)
                .build();
    }
}

package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelGroupComment;
import tournament_trail.demo.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TravelGroupCommentFixture {
    public static TravelGroupComment create(UUID id, TravelGroup travelGroup, User user, String content){
        return TravelGroupComment.builder()
                .id(id)
                .travelGroup(travelGroup)
                .hidden(false)
                .author(user)
                .content(content)
                .build();
    }
    public static List<TravelGroupComment> createList(int listSize, TravelGroup travelGroup, User user, String content){
        List<TravelGroupComment> list = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            list.add(TravelGroupCommentFixture.create(UUID.randomUUID(), travelGroup, user, content));
        }
        return list;
    }
}

package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.TravelGroupStatus;

import java.util.UUID;

public class TravelGroupFixture {
    public static TravelGroup createWithCancelledStatus(UUID id){
        return TravelGroup.builder()
                .id(id)
                .status(TravelGroupStatus.CANCELLED)
                .build();
    }

    public static TravelGroup create(UUID id, UUID userId){
        return TravelGroup.builder()
                .id(id)
                .status(TravelGroupStatus.OPEN)
                .owner(UserFixture.createUser(userId))
                .build();
    }

    public static TravelGroup createWithIdOwnerStatusMaxMembers(UUID id, UUID ownerId
            , TravelGroupStatus status, int maxMembers){
        return TravelGroup.builder()
                .id(id)
                .owner(UserFixture.createUser(ownerId))
                .status(status)
                .maximumMembers(maxMembers)
                .build();
    }
}

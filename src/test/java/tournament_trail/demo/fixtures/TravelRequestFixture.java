package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.enums.TravelRequestStatus;

import java.util.List;

public class TravelRequestFixture {
    public static TravelRequest create() {
        return TravelRequest.builder()
                .applicant(UserFixture.createUser())
                .status(TravelRequestStatus.APPROVED)
                .travelGroup(TravelGroupFixture.create())
                .build();
    }

    public static TravelRequest createWithPendingStatus() {
        return TravelRequest.builder()
                .applicant(UserFixture.createUser())
                .status(TravelRequestStatus.PENDING)
                .travelGroup(TravelGroupFixture.create())
                .build();
    }

    public static List<TravelRequest> createList() {
        TravelRequest first = TravelRequestFixture.create();
        TravelRequest second = TravelRequestFixture.create();
        second.setTravelGroup(first.getTravelGroup());
        return  List.of(first, second);

    }
}

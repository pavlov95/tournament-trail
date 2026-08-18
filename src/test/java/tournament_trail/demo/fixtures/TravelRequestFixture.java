package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.TravelRequestStatus;

import java.time.LocalDateTime;
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

    public static TravelRequest createWithPendingStatus(User applicant, TravelGroup travelGroup) {
        return TravelRequest.builder()
                .applicant(applicant)
                .status(TravelRequestStatus.PENDING)
                .travelGroup(travelGroup)
                .message("Test")
                .requestedOn(LocalDateTime.now())
                .build();
    }

    public static TravelRequest createWithApprovedStatus(User applicant, TravelGroup travelGroup) {
        return TravelRequest.builder()
                .applicant(applicant)
                .status(TravelRequestStatus.APPROVED)
                .travelGroup(travelGroup)
                .message("Test")
                .requestedOn(LocalDateTime.now())
                .build();
    }

    public static List<TravelRequest> createList() {
        TravelRequest first = TravelRequestFixture.create();
        TravelRequest second = TravelRequestFixture.create();
        second.setTravelGroup(first.getTravelGroup());
        return  List.of(first, second);

    }
}

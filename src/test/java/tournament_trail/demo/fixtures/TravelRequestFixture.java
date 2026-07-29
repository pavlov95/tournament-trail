package tournament_trail.demo.fixtures;

import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TravelRequestFixture {
    public static TravelRequest createWithApplicant(UUID applicantId){
        return TravelRequest.builder()
                .applicant(UserFixture.createUser(applicantId))
                .build();
    }
    public static List<TravelRequest> createList(int listSize){
        List<TravelRequest> list= new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            list.add(TravelRequestFixture.createWithApplicant(UUID.randomUUID()));
        }
        return list;
    }
}

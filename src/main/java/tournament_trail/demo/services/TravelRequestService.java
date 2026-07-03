package tournament_trail.demo.services;
import org.springframework.stereotype.Service;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.enums.TravelRequestStatus;
import tournament_trail.demo.repositories.TravelRequestRepository;

import java.util.List;
import java.util.UUID;

@Service
public class TravelRequestService {
    private final TravelRequestRepository travelRequestRepository;

    public TravelRequestService(TravelRequestRepository travelRequestRepository) {
        this.travelRequestRepository = travelRequestRepository;
    }

    public List<TravelRequest> findAcceptedRequests(UUID userId) {
        return travelRequestRepository.findAllByApplicantIdAndStatus(userId, TravelRequestStatus.APPROVED);
    }
}

package tournament_trail.demo.repositories;
import tournament_trail.demo.entities.TravelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tournament_trail.demo.entities.enums.TravelRequestStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest, UUID> {
    List<TravelRequest> findAllByApplicantIdAndStatus(UUID userId, TravelRequestStatus approved);
}

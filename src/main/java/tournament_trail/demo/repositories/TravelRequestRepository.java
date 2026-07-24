package tournament_trail.demo.repositories;
import tournament_trail.demo.entities.TravelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tournament_trail.demo.entities.enums.TravelRequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest, UUID> {
    List<TravelRequest> findAllByApplicantIdAndStatus(UUID userId, TravelRequestStatus approved);

    int countByTravelGroupIdAndStatus(UUID travelGroupId, TravelRequestStatus travelRequestStatus);

    boolean existsByTravelGroupIdAndApplicantId(UUID travelGroupId, UUID userId);

    List<TravelRequest> findAllByTravelGroupIdAndStatusOrderByRequestedOnDesc(UUID travelGroupId
            , TravelRequestStatus status);

    Optional<TravelRequest> findByIdAndTravelGroupId(UUID requestId, UUID travelGroupId);

    List<TravelRequest> findAllByTravelGroupIdAndStatusOrderByRespondedOnDesc(UUID travelGroupId
            , TravelRequestStatus status);

    boolean existsByTravelGroupIdAndApplicantIdAndStatus(UUID travelGroupId, UUID applicantId
            , TravelRequestStatus status);
}

package tournament_trail.demo.repositories;

import tournament_trail.demo.entities.TravelGroupComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TravelGroupCommentRepository extends JpaRepository<TravelGroupComment, UUID> {

    List<TravelGroupComment> findAllByTravelGroupIdAndHiddenFalseOrderByPinnedDescCreatedOnDesc(UUID travelGroupId);

    Optional<TravelGroupComment> findByIdAndTravelGroupIdAndHiddenFalse(UUID commentId, UUID travelGroupId);

    int countByTravelGroupIdAndHiddenFalse(UUID travelGroupId);
}

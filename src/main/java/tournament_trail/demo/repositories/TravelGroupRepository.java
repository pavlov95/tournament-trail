package tournament_trail.demo.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tournament_trail.demo.entities.TravelGroup;

import java.util.List;
import java.util.UUID;

@Repository
public interface TravelGroupRepository extends JpaRepository<TravelGroup, UUID> {
    List<TravelGroup> findAllByOwnerIdOrderByDepartureTimeAsc(UUID ownerId);
}

package tournament_trail.demo.repositories;
import tournament_trail.demo.entities.TravelGroupComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TravelGroupCommentRepository extends JpaRepository<TravelGroupComment, UUID> {
}

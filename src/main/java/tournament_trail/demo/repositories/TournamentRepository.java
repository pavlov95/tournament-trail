package tournament_trail.demo.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tournament_trail.demo.entities.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID>, JpaSpecificationExecutor<Tournament> {
    List<Tournament> findAllByOrderByRegistrationDeadlineAsc();

    List<Tournament> findAll(Specification<Tournament> specification, Sort startTime);
}

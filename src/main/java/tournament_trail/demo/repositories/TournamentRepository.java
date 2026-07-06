package tournament_trail.demo.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.web.dtos.TournamentOptionResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

    @Query("""
                SELECT new tournament_trail.demo.web.dtos.TournamentOptionResponse(
                    t.id,
                    t.name
                )
                FROM Tournament t
                WHERE t.status = :status
                AND t.startTime > :now
                AND LOWER(t.name) LIKE CONCAT('%', :query, '%')
                ORDER BY t.startTime ASC
            """)
    List<TournamentOptionResponse> searchTournamentOptions(
            @Param("query") String query,
            @Param("status") TournamentStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    List<Tournament> findAllByOrderByRegistrationDeadlineAsc();

    List<Tournament> findAll(Specification<Tournament> specification, Sort startTime);
}
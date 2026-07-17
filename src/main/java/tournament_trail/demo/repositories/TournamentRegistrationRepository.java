package tournament_trail.demo.repositories;

import tournament_trail.demo.entities.TournamentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tournament_trail.demo.entities.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, UUID> {

    List<TournamentRegistration> findAllByPlayerId(UUID userId);

    List<TournamentRegistration> findAllByPlayerIdAndHiddenFalseOrderByRegisteredOnDesc(UUID userId);

    int countByTournamentIdAndRegistrationStatusIn(UUID id, List<RegistrationStatus> pendingPayment);

    Optional<TournamentRegistration> findByTournamentIdAndPlayerId(UUID tournamentId, UUID playerId);

    List<TournamentRegistration> findAllByRegistrationStatusAndReservedUntilBefore(
            RegistrationStatus registrationStatus, LocalDateTime now);
}

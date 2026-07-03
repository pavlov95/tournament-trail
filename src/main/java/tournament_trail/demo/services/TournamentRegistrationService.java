package tournament_trail.demo.services;
import org.springframework.stereotype.Service;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.repositories.TournamentRegistrationRepository;

import java.util.List;
import java.util.UUID;

@Service
public class TournamentRegistrationService {
    private final TournamentRegistrationRepository tournamentRegistrationRepository;

    public TournamentRegistrationService(TournamentRegistrationRepository tournamentRegistrationRepository) {
        this.tournamentRegistrationRepository = tournamentRegistrationRepository;
    }

    public List<TournamentRegistration> getAllRegistrationsByUserId(UUID userId) {
        return tournamentRegistrationRepository.findAllByPlayerId(userId);
    }
}

package tournament_trail.demo.schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.services.TournamentRegistrationService;

@Component
public class TournamentRegistrationScheduler {
    private final TournamentRegistrationService tournamentRegistrationService;

    public TournamentRegistrationScheduler(TournamentRegistrationService tournamentRegistrationService) {
        this.tournamentRegistrationService = tournamentRegistrationService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void expirePendingPaymentRegistrations(){
        tournamentRegistrationService.expirePendingTournamentRegistrations();
    }
}

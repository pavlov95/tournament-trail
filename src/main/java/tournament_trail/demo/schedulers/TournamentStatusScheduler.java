package tournament_trail.demo.schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tournament_trail.demo.services.TournamentService;

@Component
public class TournamentStatusScheduler {
    private final TournamentService tournamentService;

    public TournamentStatusScheduler(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Europe/Sofia")
    public void changeTournamentStatus(){
        tournamentService.updateTournamentStatuses();
    }
}

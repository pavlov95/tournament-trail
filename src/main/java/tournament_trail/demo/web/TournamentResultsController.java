package tournament_trail.demo.web;

import feign.FeignException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import tournament_trail.demo.resultclient.GameRequest;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.TournamentRegistrationService;
import tournament_trail.demo.services.TournamentResultsService;
import org.springframework.web.bind.annotation.*;
import tournament_trail.demo.services.TournamentService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tournaments/{tournamentId}")
public class TournamentResultsController {

    private final TournamentResultsService tournamentResultsService;
    private final TournamentRegistrationService tournamentRegistrationService;
    private final TournamentService tournamentService;

    public TournamentResultsController(TournamentResultsService tournamentResultsService, TournamentRegistrationService tournamentRegistrationService, TournamentService tournamentService) {
        this.tournamentResultsService = tournamentResultsService;
        this.tournamentRegistrationService = tournamentRegistrationService;
        this.tournamentService = tournamentService;
    }

    @GetMapping("/results")
    public ModelAndView getTournamentResults(@PathVariable UUID tournamentId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView("tournament-results");

        boolean canUserEditGames = tournamentService.canEditGames(tournamentId, userDetails.getId(), userDetails.getRole());
        modelAndView.addObject("canUserEditGames", canUserEditGames);

        modelAndView.addObject("tournamentId", tournamentId);
        modelAndView.addObject("gameRequest", new GameRequest());
        modelAndView.addObject("players"
                , tournamentRegistrationService.getConfirmedPlayersForTournament(tournamentId));

        try {
            modelAndView.addObject("games"
                    , tournamentResultsService.getGamesByTournament(tournamentId));

            modelAndView.addObject("standings", tournamentResultsService.getStandings(tournamentId));

        } catch (FeignException exception) {
            modelAndView.addObject("games", List.of());
            modelAndView.addObject("standings", List.of());

            modelAndView.addObject("resultsError"
                    , "Tournament results service error. Status: " + exception.status());

            System.out.println("Feign error from tournament-results-service: " + exception.status());
            System.out.println(exception.getMessage());
        }

        return modelAndView;
    }

    @PostMapping("/games")
    public String createGame(@PathVariable UUID tournamentId, @ModelAttribute GameRequest gameRequest) {

        tournamentRegistrationService.populatePlayerUsernames(tournamentId, gameRequest);

        tournamentResultsService.createGame(tournamentId, gameRequest);

        return "redirect:/tournaments/" + tournamentId + "/results";
    }

    @PostMapping("/games/{gameId}/edit")
    public String updateGame(@PathVariable UUID tournamentId, @PathVariable UUID gameId
            , @ModelAttribute GameRequest gameRequest) {

        tournamentRegistrationService.populatePlayerUsernames(tournamentId, gameRequest);

        tournamentResultsService.updateGame(tournamentId, gameId, gameRequest);

        return "redirect:/tournaments/" + tournamentId + "/results";
    }

    @PostMapping("/games/{gameId}/delete")
    public String deleteGame(@PathVariable UUID tournamentId, @PathVariable UUID gameId) {

        tournamentResultsService.deleteGame(tournamentId, gameId);

        return "redirect:/tournaments/" + tournamentId + "/results";
    }
}
package tournament_trail.demo.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TimeControl;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.TournamentService;
import tournament_trail.demo.web.dtos.CreateTournamentRequest;
import tournament_trail.demo.web.dtos.TournamentSearchRequest;

import java.util.UUID;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public ModelAndView getTournaments(@ModelAttribute("searchRequest")
                                           TournamentSearchRequest searchRequest) {
        ModelAndView modelAndView = new ModelAndView("tournaments");
        modelAndView.addObject("tournaments",
                tournamentService.searchTournaments(searchRequest));

        modelAndView.addObject("createTournamentRequest", new CreateTournamentRequest());
        addCommonPageData(modelAndView);

        return modelAndView;
    }

    @PostMapping
    public ModelAndView createTournament(@Valid @ModelAttribute("createTournamentRequest")
            CreateTournamentRequest createTournamentRequest, BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationUserDetails userDetails,
                                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("tournaments");

            modelAndView.addObject("tournaments", tournamentService.searchTournaments(
                            new TournamentSearchRequest()));
            modelAndView.addObject("searchRequest", new TournamentSearchRequest());
            addCommonPageData(modelAndView);
            return modelAndView;
        }

        UUID userId = userDetails.getId();
        tournamentService.createTournament(createTournamentRequest, userId);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Tournament created successfully.");
        return new ModelAndView("redirect:/tournaments#create-tournament");
    }

    private void addCommonPageData(ModelAndView modelAndView) {
        modelAndView.addObject("timeControls", TimeControl.values());

        modelAndView.addObject("currencies", CurrencyCode.values());
    }
}
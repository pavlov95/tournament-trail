package tournament_trail.demo.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TimeControl;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.TournamentRegistrationService;
import tournament_trail.demo.services.TournamentService;
import tournament_trail.demo.web.dtos.OrganiserNoteRequest;
import tournament_trail.demo.web.dtos.TournamentOptionResponse;
import tournament_trail.demo.web.dtos.TournamentRequest;
import tournament_trail.demo.web.dtos.TournamentSearchRequest;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentRegistrationService tournamentRegistrationService;

    public TournamentController(TournamentService tournamentService, TournamentRegistrationService tournamentRegistrationService) {
        this.tournamentService = tournamentService;
        this.tournamentRegistrationService = tournamentRegistrationService;
    }

    @GetMapping
    public ModelAndView getTournaments(@ModelAttribute("searchRequest")
                                       TournamentSearchRequest searchRequest) {
        ModelAndView modelAndView = new ModelAndView("tournaments");
        modelAndView.addObject("tournaments",
                tournamentService.searchTournaments(searchRequest));

        return modelAndView;
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('TOURNAMENT_CREATE')")
    public ModelAndView getCreateTournamentPage() {
        ModelAndView modelAndView = new ModelAndView("tournament-create");
        modelAndView.addObject("tournamentRequest", new TournamentRequest());
        addCommonPageData(modelAndView);
        return modelAndView;
    }


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('TOURNAMENT_CREATE')")
    public ModelAndView createTournament(
            @Valid @ModelAttribute("tournamentRequest")
            TournamentRequest tournamentRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("tournament-create");

            modelAndView.addObject("tournaments",
                    tournamentService.searchTournaments(new TournamentSearchRequest()));

            modelAndView.addObject("searchRequest", new TournamentSearchRequest());
            addCommonPageData(modelAndView);

            return modelAndView;
        }

        Tournament tournament = tournamentService.createTournament(tournamentRequest, userDetails.getId());

        redirectAttributes.addFlashAttribute("successMessage"
                , "Tournament created successfully.");

        return new ModelAndView("redirect:/tournaments/" + tournament.getId());
    }

    @PutMapping("/{tournamentId}")
    @PreAuthorize("hasAuthority('TOURNAMENT_EDIT_OWN') or hasRole('ADMIN')")
    public ModelAndView editTournament(
            @PathVariable UUID tournamentId,
            @Valid @ModelAttribute("tournamentRequest")
            TournamentRequest tournamentRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationUserDetails userDetails) {
        Tournament tournament = tournamentService.findById(tournamentId);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("tournament-details");

            modelAndView.addObject("tournament", tournament);
            modelAndView.addObject("canEdit", true);
            addCommonPageData(modelAndView);

            return modelAndView;
        }

        tournamentService.editTournament(tournamentRequest, tournamentId, userDetails.getId()
                , userDetails.getRole());

        return new ModelAndView("redirect:/tournaments/" + tournamentId);
    }

    @GetMapping("/{tournamentId}")
    public ModelAndView getTournament(
            @PathVariable UUID tournamentId,
            @AuthenticationPrincipal AuthenticationUserDetails userDetails) {
        Tournament tournament = tournamentService.findById(tournamentId);

        ModelAndView modelAndView = new ModelAndView("tournament-details");
        modelAndView.addObject("tournament", tournament);
        modelAndView.addObject("tournamentRequest",
                tournamentService.mapToTournamentRequest(tournament));
        boolean canEdit = false;

        if (userDetails != null) {
            canEdit = tournamentService.canEditTournament(tournament, userDetails.getId(), userDetails.getRole());
        }
        modelAndView.addObject("canEdit", canEdit);
        addCommonPageData(modelAndView);

        return modelAndView;
    }

    @PatchMapping("/{tournamentId}")
    @PreAuthorize("hasAuthority('TOURNAMENT_EDIT_OWN') or hasRole('ADMIN')")
    public ModelAndView updateTournamentStatus(
            @PathVariable UUID tournamentId,
            @RequestParam("status") TournamentStatus status,
            @AuthenticationPrincipal AuthenticationUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        tournamentService.updateTournamentStatus(tournamentId, status, userDetails.getId(), userDetails.getRole());

        if (status == TournamentStatus.PUBLISHED) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tournament published successfully.");
        }

        if (status == TournamentStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tournament cancelled successfully.");
        }

        return new ModelAndView("redirect:/tournaments/" + tournamentId);
    }

    @GetMapping("/autocomplete")
    @ResponseBody
    public List<TournamentOptionResponse> autocompleteTournaments(@RequestParam("query") String query) {
        return tournamentService.searchTournamentOptions(query);
    }

    @GetMapping("/{tournamentId}/registrations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ModelAndView getRegistrationsForTournament(@PathVariable UUID tournamentId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        ModelAndView modelAndView = new ModelAndView("tournament-registration-management");
        List<TournamentRegistration> registrations = tournamentRegistrationService.getRegistrationsForTournamentManagement
                (tournamentId, userDetails.getId(), userDetails.getRole());
        modelAndView.addObject("registrations", registrations);
        modelAndView.addObject("organiserNoteRequest", new OrganiserNoteRequest());
        return modelAndView;
    }

    @PatchMapping("/{tournamentId}/registrations/{registrationId}/approve-payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ModelAndView approveRegistration(@PathVariable UUID tournamentId, @PathVariable UUID registrationId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails
            , RedirectAttributes redirectAttributes) {

        tournamentRegistrationService
                .approvePayment(registrationId, userDetails.getId(), userDetails.getRole());

        redirectAttributes.addFlashAttribute("successMessage", "Payment approved.");

        return new ModelAndView("redirect:/tournaments/" + tournamentId + "/registrations");
    }

    @PatchMapping("/{tournamentId}/registrations/{registrationId}/reject-payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISER')")
    public ModelAndView rejectRegistration(@PathVariable UUID tournamentId, @PathVariable UUID registrationId
            , @Valid @ModelAttribute("organiserNoteRequest") OrganiserNoteRequest organiserNoteRequest
            , BindingResult bindingResult, @AuthenticationPrincipal AuthenticationUserDetails userDetails
            , RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("tournament-registration-management");
            modelAndView.addObject("organiserNoteRequest",organiserNoteRequest);
            List<TournamentRegistration> registrations = tournamentRegistrationService.getRegistrationsForTournamentManagement
                    (tournamentId, userDetails.getId(), userDetails.getRole());
            modelAndView.addObject("registrations", registrations);
            return modelAndView;
        }

        tournamentRegistrationService.rejectPayment(registrationId, userDetails.getId()
                , userDetails.getRole(), organiserNoteRequest.getOrganiserNote());

        redirectAttributes.addFlashAttribute("successMessage", "Payment rejected.");

        return new ModelAndView("redirect:/tournaments/" + tournamentId + "/registrations");
    }


    private void addCommonPageData(ModelAndView modelAndView) {
        modelAndView.addObject("timeControls", TimeControl.values());
        modelAndView.addObject("currencies", CurrencyCode.values());
    }


}
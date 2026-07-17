package tournament_trail.demo.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.TournamentRegistrationService;
import tournament_trail.demo.web.dtos.PaymentRequest;

import java.util.UUID;

@Controller
@RequestMapping("/registrations")
public class TournamentRegistrationController {
    private final TournamentRegistrationService tournamentRegistrationService;

    public TournamentRegistrationController(TournamentRegistrationService tournamentRegistrationService) {
        this.tournamentRegistrationService = tournamentRegistrationService;
    }

    @GetMapping()
    public ModelAndView viewTournamentRegistrations(@AuthenticationPrincipal AuthenticationUserDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView("tournament-registrations");
        UUID userId = userDetails.getId();
        modelAndView.addObject("registrations",
                tournamentRegistrationService.getAllUnhiddenRegistrationsByUserId(userId));

        return modelAndView;
    }
    @PostMapping()
    public ModelAndView registerForTournament(@RequestParam UUID tournamentId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails){

        TournamentRegistration registration =
                tournamentRegistrationService.create(userDetails.getId(), tournamentId);

        return new ModelAndView("redirect:/registrations/" + registration.getId());
    }

    @GetMapping("/{id}")
    public ModelAndView getRegistrationDetails(@PathVariable UUID id
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails){

        TournamentRegistration registration = tournamentRegistrationService
                .getRegistrationIfOwnerOrAdmin(userDetails.getId(), id, userDetails.getRole());

        ModelAndView modelAndView = new ModelAndView("tournament-registration");
        modelAndView.addObject("registration", registration);
        modelAndView.addObject("paymentRequest", new PaymentRequest());
        return modelAndView;
    }

    @PatchMapping("/{id}/cancel")
    public ModelAndView cancelRegistration(@PathVariable UUID id
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails){

        tournamentRegistrationService.cancelRegistration(id, userDetails.getId(), userDetails.getRole());

        return new ModelAndView("redirect:/registrations/" + id);
    }

    @DeleteMapping("/{id}")
    public ModelAndView hideRegistration(@PathVariable UUID id
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails){

        tournamentRegistrationService.hideRegistration(id, userDetails.getId());

        return new ModelAndView("redirect:/registrations");
    }

    @PatchMapping("/{id}/payment")
    public ModelAndView addPayment(@PathVariable UUID id
            , @Valid PaymentRequest paymentRequest, BindingResult bindingResult
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            TournamentRegistration registration = tournamentRegistrationService
                    .getRegistrationIfOwnerOrAdmin(userDetails.getId(), id, userDetails.getRole());

            ModelAndView modelAndView = new ModelAndView("tournament-registration");
            modelAndView.addObject("registration", registration);
            modelAndView.addObject("paymentRequest", paymentRequest);

            return modelAndView;
        }

        tournamentRegistrationService.addPayment(id, paymentRequest, userDetails.getId() );
        redirectAttributes.addFlashAttribute("successMessage"
                , "Payment reference submitted successfully.");

        return new ModelAndView("redirect:/registrations/" + id);
    }
}

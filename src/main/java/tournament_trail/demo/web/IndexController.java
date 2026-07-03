package tournament_trail.demo.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.VerificationToken;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.*;
import tournament_trail.demo.web.dtos.LoginRequest;
import tournament_trail.demo.web.dtos.RegisterRequest;
import java.util.List;
import java.util.UUID;


@Controller
public class IndexController {
    ;
    private final TournamentRegistrationService tournamentRegistrationService;
    private final TravelGroupService travelGroupService;
    private final TravelRequestService travelRequestService;
    private final UserService userService;
    private final VerificationTokenService verificationTokenService;


    public IndexController(TournamentRegistrationService tournamentRegistrationService,
                           TravelGroupService travelGroupService,
                           TravelRequestService travelRequestService, UserService userService, VerificationTokenService verificationTokenService) {
        this.tournamentRegistrationService = tournamentRegistrationService;
        this.travelGroupService = travelGroupService;
        this.travelRequestService = travelRequestService;
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
    }

    @GetMapping()
    public ModelAndView getIndexPage() {
        return new ModelAndView("index");
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage() {
        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("loginRequest", new LoginRequest());
        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {
        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }
        userService.register(registerRequest);
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationUserDetails userDetails) {
        UUID userId = userDetails.getId();
        List<TournamentRegistration> registrations = tournamentRegistrationService
                        .getAllRegistrationsByUserId(userId);

        List<TravelGroup> travelGroups = travelGroupService.getTravelGroupsByUser(userId);

        List<TravelRequest> travelRequests = travelRequestService.findAcceptedRequests(userId);

        ModelAndView modelAndView = new ModelAndView("home");

        modelAndView.addObject("registrations", registrations);
        modelAndView.addObject("travelGroups", travelGroups);
        modelAndView.addObject("travelRequests", travelRequests);
        modelAndView.addObject("currentRole", userDetails.getRole());
        return modelAndView;
    }

    @GetMapping("/verify")
    public ModelAndView verifyAccount(@RequestParam String token) {
        VerificationToken verificationToken = verificationTokenService.verifyToken(token);
        userService.enableAccount(verificationToken);
        verificationTokenService.delete(verificationToken);
        ModelAndView modelAndView = new ModelAndView("redirect:/login");

        modelAndView.addObject("verified", true);
        return modelAndView;

    }

    @GetMapping("/about")
    public ModelAndView getAboutPage(){
        return new ModelAndView("about");
    }

    @GetMapping("/contact")
    public ModelAndView getContactPage(){
        return new ModelAndView("contact");
    }

}

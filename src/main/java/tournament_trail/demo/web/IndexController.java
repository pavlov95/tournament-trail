package tournament_trail.demo.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import tournament_trail.demo.services.RegistrationService;
import tournament_trail.demo.web.dtos.LoginRequest;
import tournament_trail.demo.web.dtos.RegisterRequest;


@Controller
public class IndexController {
    private final RegistrationService registrationService;

    public IndexController(RegistrationService registrationService) {
        this.registrationService = registrationService;
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
        registrationService.register(registerRequest);
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView getHomePage() {
        return new ModelAndView("home");
    }

    @GetMapping("/verify")
    public ModelAndView verifyAccount(@RequestParam String token) {
        registrationService.verifyToken(token);
        ModelAndView modelAndView = new ModelAndView("redirect:/login");

        modelAndView.addObject("verified", true);
        return modelAndView;

    }
}

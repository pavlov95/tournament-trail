package tournament_trail.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.VerificationToken;
import tournament_trail.demo.web.dtos.RegisterRequest;


@Service
public class RegistrationService {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;

    public RegistrationService(
            UserService userService,
            VerificationTokenService verificationTokenService,
            EmailService emailService) {

        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.emailService = emailService;
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        User user = userService.register(registerRequest);
        VerificationToken verificationToken = verificationTokenService.generateToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());
    }

    @Transactional
    public void verifyToken(String token){
        VerificationToken verificationToken = verificationTokenService.verifyToken(token);
        userService.enableAccount(verificationToken);
        verificationTokenService.delete(verificationToken);
    }
}
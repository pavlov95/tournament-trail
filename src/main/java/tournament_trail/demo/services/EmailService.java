package tournament_trail.demo.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final VerificationTokenService verificationTokenService;
    private final JavaMailSender mailSender;
    private final String VERIFICATION_LINK = "http://localhost:8080/verify?token=";

    public EmailService(VerificationTokenService verificationTokenService, JavaMailSender mailSender) {
        this.verificationTokenService = verificationTokenService;
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String recipient, String tokenValue) {
        SimpleMailMessage message = new SimpleMailMessage();
        String verificationUrl = VERIFICATION_LINK+tokenValue;

        message.setTo(recipient);
        message.setSubject("Verify your TournamentTrail account");
        message.setText(
                """
                        Welcome to TournamentTrail.
                        
                        Verify your account by opening this link:
                        
                        %s
                        
                        The link expires in 1 hour.
                        """.formatted(verificationUrl));

        mailSender.send(message);
    }




//    public void sendSuccessfulTournamentCreation(){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(rec);
//    }
}
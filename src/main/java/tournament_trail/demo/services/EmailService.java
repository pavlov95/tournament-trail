package tournament_trail.demo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.public-url}")
    private final String publicUrl;

    public EmailService(JavaMailSender mailSender, @Value("${app.public-url}") String publicUrl) {
        this.mailSender = mailSender;
        this.publicUrl = publicUrl;
    }

    public void sendVerificationEmail(String recipient, String tokenValue) {
        SimpleMailMessage message = new SimpleMailMessage();
        String verificationUrl = publicUrl + "/verify?token=" + tokenValue;

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
}
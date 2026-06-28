package tournament_trail.demo.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String recipient, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(recipient);
        message.setSubject("Verify your TournamentTrail account");
        message.setText(
                """
                Welcome to TournamentTrail.

                Verify your account by opening this link:

                %s

                The link expires in 24 hours.
                """.formatted(verificationLink)
        );

        mailSender.send(message);
    }
}
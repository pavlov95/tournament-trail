package tournament_trail.demo.services;

import org.springframework.stereotype.Service;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.VerificationToken;
import tournament_trail.demo.exceptions.ExpiredVerificationTokenException;
import tournament_trail.demo.exceptions.InvalidVerificationTokenException;
import tournament_trail.demo.repositories.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }


    public VerificationToken generateToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    public void update(VerificationToken verificationToken) {
        verificationTokenRepository.save(verificationToken);
    }

    public Optional<VerificationToken> findByToken(String tokenValue) {
        return verificationTokenRepository.findByToken(tokenValue);
    }

    public void delete(VerificationToken token) {
        verificationTokenRepository.delete(token);
    }

    public VerificationToken verifyToken(String tokenValue) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(InvalidVerificationTokenException::new);

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredVerificationTokenException();
        }
        return verificationToken;
    }
}

package tournament_trail.demo.services;

import org.springframework.stereotype.Service;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.VerificationToken;
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


    public VerificationToken generateToken(User user){
        String tokenValue = UUID.randomUUID().toString();

       return VerificationToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    public void update(VerificationToken verificationToken){
        verificationTokenRepository.save(verificationToken);
    }

    public Optional<VerificationToken> findByToken(String tokenValue) {
        return verificationTokenRepository.findByToken(tokenValue);
    }

    public void delete(VerificationToken token) {
        verificationTokenRepository.delete(token);
    }
}

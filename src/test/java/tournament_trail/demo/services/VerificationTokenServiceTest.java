package tournament_trail.demo.services;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.VerificationToken;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.exceptions.ExpiredVerificationTokenException;
import tournament_trail.demo.exceptions.InvalidVerificationTokenException;
import tournament_trail.demo.repositories.VerificationTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceTest {
    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Captor
    private ArgumentCaptor<VerificationToken> captor;

    @InjectMocks
    private VerificationTokenService verificationTokenService;

    @Test
    public void generateToken_shouldReturnValidVerificationToken() {
        User user = User.builder()
                .username("dimitar")
                .email("example@example.com")
                .role(Role.PLAYER)
                .password("Password1")
                .build();

        LocalDateTime beforeGeneration = LocalDateTime.now();

        VerificationToken result = verificationTokenService.generateToken(user);

        LocalDateTime afterGeneration = LocalDateTime.now();

        verify(verificationTokenRepository).save(captor.capture());

        VerificationToken savedToken = captor.getValue();

        assertAll(
                () -> assertNotNull(savedToken.getToken()),
                () -> assertDoesNotThrow(() -> UUID.fromString(savedToken.getToken())),
                () -> assertSame(user, savedToken.getUser()),
                () -> assertNotNull(savedToken.getExpiresAt()),
                () -> assertFalse(savedToken.getExpiresAt()
                        .isBefore(beforeGeneration.plusHours(1))),
                () -> assertFalse(savedToken.getExpiresAt()
                        .isAfter(afterGeneration.plusHours(1))),
                () -> assertSame(savedToken, result)
        );
    }

    @Test
    public void update_shouldSaveToken() {
        VerificationToken verificationToken = VerificationToken.builder()
                .token("RandomToken")
                .build();

        verificationTokenService.update(verificationToken);

        verify(verificationTokenRepository).save(verificationToken);
    }

    @Test
    public void verifyToken_shouldThrowInvalidVerificationTokenException() {
        when(verificationTokenRepository.findByToken("az")).thenReturn(Optional.empty());

        InvalidVerificationTokenException exception = assertThrows(InvalidVerificationTokenException.class,
                () -> verificationTokenService.verifyToken("az"));
    }

    @Test
    public void verifyToken_shouldThrowExpiredVerificationTokenException() {
        VerificationToken verificationToken = VerificationToken.builder()
                .token("az")
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .build();
        when(verificationTokenRepository.findByToken("az")).thenReturn(Optional.of(verificationToken));

         assertThrowsExactly(ExpiredVerificationTokenException.class
                , ()->verificationTokenService.verifyToken("az"));

        verify(verificationTokenRepository).findByToken("az");
    }
    @Test
    public void verifyToken_shouldReturnVerificationToken(){
        VerificationToken verificationToken = VerificationToken.builder()
                .token("az")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(verificationTokenRepository.findByToken("az")).thenReturn(Optional.of(verificationToken));

        VerificationToken result = verificationTokenService.verifyToken("az");
        assertEquals("az", result.getToken());
        assertTrue(LocalDateTime.now().isBefore(verificationToken.getExpiresAt()));
        verify(verificationTokenRepository).findByToken("az");
    }

}

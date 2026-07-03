package tournament_trail.demo.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.VerificationToken;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.exceptions.UserDoesNotExist;
import tournament_trail.demo.repositories.UserRepository;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.web.dtos.RegisterRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, VerificationTokenService verificationTokenService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenService = verificationTokenService;
        this.emailService = emailService;
    }


    @Transactional
    public void register(RegisterRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .role(Role.PLAYER)
                .enabled(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        userRepository.save(user);
        VerificationToken verificationToken = verificationTokenService.generateToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());
    }

    @Transactional
    public void enableAccount(VerificationToken verificationToken) {
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return AuthenticationUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }


    public User findByUsername(String username) {
       return userRepository.findByUsername(username).orElseThrow(
               () -> new UsernameNotFoundException(username));
    }

    public User findById(UUID userID) {
        return userRepository.findById(userID).orElseThrow(UserDoesNotExist::new);

    }
}

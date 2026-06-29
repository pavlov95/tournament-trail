package tournament_trail.demo.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tournament_trail.demo.entities.VerificationToken;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.repositories.UserRepository;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.web.dtos.RegisterRequest;

import java.time.LocalDateTime;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public User register(RegisterRequest registerRequest) {
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
        return user;

    }

    public void enableAccount(VerificationToken verificationToken) {
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return AuthenticationUserDetails.builder()
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }


}

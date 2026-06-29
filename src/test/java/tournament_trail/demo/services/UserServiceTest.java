package tournament_trail.demo.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.VerificationToken;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.repositories.UserRepository;
import tournament_trail.demo.web.dtos.RegisterRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> captor;

    @Test
    void register_shouldEncodePasswordAndSaveDisabledPlayer() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("dimitar");
        request.setEmail("example@example.com");
        request.setPassword("Password1");

        when(passwordEncoder.encode("Password1")).thenReturn("encodedPassword");

        User result = userService.register(request);

        verify(passwordEncoder).encode("Password1");
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertAll(
                () -> assertEquals("dimitar", savedUser.getUsername()),
                () -> assertEquals("example@example.com", savedUser.getEmail()),
                () -> assertEquals("encodedPassword", savedUser.getPassword()),
                () -> assertNotEquals("Password1", savedUser.getPassword()),
                () -> assertEquals(Role.PLAYER, savedUser.getRole()),
                () -> assertFalse(savedUser.isEnabled()),
                () -> assertNotNull(savedUser.getCreatedOn()),
                () -> assertNotNull(savedUser.getUpdatedOn()),
                () -> assertSame(savedUser, result)
        );
    }

    @Test
    void enableAccount_shouldEnableAccountAndSaveUser() {
        User user = User.builder()
                .enabled(false)
                .build();

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .build();

        userService.enableAccount(verificationToken);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertTrue(savedUser.isEnabled());
        assertSame(user, savedUser);
    }

    @Test
    public void loadUserByUsername_shouldReturnAuthenticatedUser(){
        User user = User.builder()
                .username("dimitar")
                .role(Role.PLAYER)
                .enabled(false)
                .password("encodedPassword")
                .email("example@example.com")
                .build();

        when(userRepository.findByUsername("dimitar")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("dimitar");

        assertAll(
                ()->assertEquals("dimitar", userDetails.getUsername()),
                ()->assertFalse( userDetails.isEnabled()),
                ()->assertEquals("encodedPassword", userDetails.getPassword()),
                ()->assertTrue(userDetails.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_PLAYER")
                        ))
        );
        verify(userRepository).findByUsername("dimitar");
    }

    @Test
    public void loadUserByUsername_shouldReturnException(){
        when(userRepository.findByUsername("dimitar")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("dimitar"));

        assertEquals("dimitar", exception.getMessage());

        verify(userRepository).findByUsername("dimitar");
    }
}
//@Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
//        return AuthenticationUserDetails.builder()
//                .username(user.getUsername())
//                .enabled(user.isEnabled())
//                .password(user.getPassword())
//                .role(user.getRole())
//                .build();
//    }
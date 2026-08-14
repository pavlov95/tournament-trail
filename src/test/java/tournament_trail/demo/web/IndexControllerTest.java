package tournament_trail.demo.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tournament_trail.demo.entities.*;
import tournament_trail.demo.fixtures.AuthenticationUserDetailsFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.fixtures.dtos.RegisterRequestFixture;
import tournament_trail.demo.repositories.UserRepository;
import tournament_trail.demo.repositories.VerificationTokenRepository;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @MockitoBean
    private TournamentRegistrationService tournamentRegistrationService;

    @MockitoBean
    private TravelGroupService travelGroupService;

    @MockitoBean
    private TravelRequestService travelRequestService;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        verificationTokenRepository.deleteAll();
    }

    @Test
    public void getIndexPage_shouldReturnStatusOk() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void getLoginPage_shouldReturnStatusOk() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void getRegisterPage_shouldReturnStatusOk() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    public void register_whenInvalidData_shouldReturnRegisterViewWithValidationErrors() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", RegisterRequestFixture.TEST_INVALID_USERNAME)
                        .param("password", RegisterRequestFixture.TEST_INVALID_PASSWORD)
                        .param("email", RegisterRequestFixture.TEST_INVALID_EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors(
                        "registerRequest",
                        "username",
                        "password",
                        "email"));
        assertEquals(0, userRepository.count());
    }

    @Test
    public void register_whenValidData_shouldRegisterUserAndRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", RegisterRequestFixture.TEST_USERNAME)
                        .param("password", RegisterRequestFixture.TEST_PASSWORD)
                        .param("email", RegisterRequestFixture.TEST_EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"));

        assertEquals(1, userRepository.count());

        User user = userRepository.findAll().get(0);
        assertFalse(user.isEnabled());
        assertEquals(RegisterRequestFixture.TEST_USERNAME, user.getUsername());
        assertEquals(RegisterRequestFixture.TEST_EMAIL, user.getEmail());
        assertEquals(1, verificationTokenRepository.count());
    }

    @Test
    public void getHomePage_whenAuthenticated_shouldReturnHomeView() throws Exception {
        AuthenticationUserDetails userDetails = AuthenticationUserDetailsFixture.create();
        UUID  userId = userDetails.getId();

        when(tournamentRegistrationService.getAllRegistrationsByUserId(userDetails.getId()))
                .thenReturn(List.of());

        when(travelGroupService.getTravelGroupsByUser(userId))
                .thenReturn(List.of());

        when(travelRequestService.findAcceptedRequests(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/home")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("registrations", List.of()))
                .andExpect(model().attribute("travelGroups", List.of()))
                .andExpect(model().attribute("travelRequests", List.of()))
                .andExpect(model().attribute("currentRole", userDetails.getRole()));

        verify(tournamentRegistrationService).getAllRegistrationsByUserId(userId);
        verify(travelGroupService).getTravelGroupsByUser(userId);
        verify(travelRequestService).findAcceptedRequests(userId);
    }

    @Test
    public void getAboutPage_shouldReturnAboutPage() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }
    @Test
    public void getContactPage_shouldReturnContactPage() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
    }

    @Test
    public void verifyAccount_shouldVerifyAccountSuccessfully() throws Exception {
        User user = UserFixture.createWithAllFields();
        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token("Test token")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        userRepository.save(user);
        verificationTokenRepository.save(verificationToken);

        mockMvc.perform(get("/verify")
                        .param("token", verificationToken.getToken()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"));

        assertEquals(0, verificationTokenRepository.count());
        assertEquals(1, userRepository.count());
        User result = userRepository.findAll().get(0);

        assertTrue(result.isEnabled());
    }

    @Test
    public void getHomePage_whenAnonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection());
    }
}
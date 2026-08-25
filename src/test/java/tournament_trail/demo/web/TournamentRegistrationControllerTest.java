package tournament_trail.demo.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.PaymentStatus;
import tournament_trail.demo.entities.enums.RegistrationStatus;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.fixtures.AuthenticationUserDetailsFixture;
import tournament_trail.demo.fixtures.TournamentFixture;
import tournament_trail.demo.fixtures.TournamentRegistrationFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.*;
import tournament_trail.demo.security.AuthenticationUserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TournamentRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    @Autowired
    private TravelGroupRepository travelGroupRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TravelGroupCommentRepository travelGroupCommentRepository;

    @Autowired
    private TravelRequestRepository travelRequestRepository;

    private AuthenticationUserDetails organiserDetails;
    private Tournament tournament;
    private User applicant;
    private AuthenticationUserDetails applicantDetails;

    @BeforeEach
    public void setUp() {
        travelGroupCommentRepository.deleteAllInBatch();
        travelRequestRepository.deleteAllInBatch();
        tournamentRegistrationRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        travelGroupRepository.deleteAllInBatch();
        tournamentRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        User organiser = UserFixture.createWithAllFields();
        applicant = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(organiser);
        userRepository.save(applicant);
        organiserDetails = AuthenticationUserDetailsFixture.createFromUser(organiser);
        applicantDetails = AuthenticationUserDetailsFixture.createFromUser(applicant);

        tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

    }

    @Test
    public void viewTournamentRegistration_whenAuthenticated_shouldReturnRegistrations() throws Exception {
        mockMvc.perform(get("/registrations")
                        .with(user(organiserDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-registrations"));
    }

    @Test
    public void viewTournamentRegistration_whenNotAuthenticate_shouldRedirect() throws Exception {
        mockMvc.perform(get("/registrations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    public void registerForTournament_whenAuthenticatedAndTournamentNotFree_shouldCreatePendingRegistration() throws Exception {
        mockMvc.perform(post("/registrations")
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("tournamentId", tournament.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/registrations/*"));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);
        assertEquals(applicant.getId(), result.getPlayer().getId());
        assertEquals(tournament.getId(), result.getTournament().getId());
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        assertEquals(RegistrationStatus.PENDING_PAYMENT, result.getRegistrationStatus());
        assertNotNull(result.getRegisteredOn());
        assertNotNull(result.getUpdatedOn());
        assertFalse(result.isHidden());
    }

    @Test
    public void registerForTournament_whenAuthenticatedAndTournamentFree_shouldCreateConfirmedRegistration() throws Exception {
        tournament.setEntryFee(BigDecimal.ZERO);
        tournamentRepository.save(tournament);

        mockMvc.perform(post("/registrations")
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("tournamentId", tournament.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/registrations/*"));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);
        assertEquals(applicant.getId(), result.getPlayer().getId());
        assertEquals(tournament.getId(), result.getTournament().getId());
        assertEquals(PaymentStatus.NOT_REQUIRED, result.getPaymentStatus());
        assertEquals(RegistrationStatus.CONFIRMED, result.getRegistrationStatus());
        assertNotNull(result.getRegisteredOn());
        assertNotNull(result.getUpdatedOn());
        assertFalse(result.isHidden());
    }

    @Test
    public void registerForTournament_whenAuthenticatedAndTournamentNotPublished_shouldReturnBadRequest() throws Exception {
        tournament.setStatus(TournamentStatus.CANCELLED);
        tournamentRepository.save(tournament);

        mockMvc.perform(post("/registrations")
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("tournamentId", tournament.getId().toString()))
                .andExpect(status().isBadRequest());

        assertEquals(0, tournamentRegistrationRepository.count());
    }

    @Test
    public void registerForTournament_whenAuthenticatedAndTournamentFull_shouldReturnBadRequest() throws Exception {
        tournament.setMaximumParticipants(0);
        tournamentRepository.save(tournament);

        mockMvc.perform(post("/registrations")
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("tournamentId", tournament.getId().toString()))
                .andExpect(status().isBadRequest());

        assertEquals(0, tournamentRegistrationRepository.count());
    }

    @Test
    public void registerForTournament_whenAuthenticatedAndTournamentStarted_shouldReturnBadRequest() throws Exception {
        tournament.setStartTime(LocalDateTime.now().minusSeconds(1));
        tournamentRepository.save(tournament);

        mockMvc.perform(post("/registrations")
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("tournamentId", tournament.getId().toString()))
                .andExpect(status().isBadRequest());

        assertEquals(0, tournamentRegistrationRepository.count());
    }

    @Test
    public void registerForTournament_whenAuthenticatedAndTournamentRegistrationEnded_shouldReturnBadRequest() throws Exception {
        tournament.setRegistrationDeadline(LocalDateTime.now().minusSeconds(1));
        tournamentRepository.save(tournament);

        mockMvc.perform(post("/registrations")
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("tournamentId", tournament.getId().toString()))
                .andExpect(status().isForbidden());

        assertEquals(0, tournamentRegistrationRepository.count());
    }

    @Test
    public void registerForTournament_whenRejectedRegistrationExists_shouldReturnBadRequestAndNotChangeRegistration() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusRejectedAndPaymentStatusConfirmed(applicant, tournament);
        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(post("/registrations")
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("tournamentId", tournament.getId().toString()))
                .andExpect(status().isBadRequest());

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);
        assertEquals(applicant.getId(), result.getPlayer().getId());
        assertEquals(tournament.getId(), result.getTournament().getId());
        assertEquals(PaymentStatus.CONFIRMED, result.getPaymentStatus());
        assertEquals(RegistrationStatus.REJECTED, result.getRegistrationStatus());
        assertNotNull(result.getRegisteredOn());
        assertNotNull(result.getUpdatedOn());
        assertFalse(result.isHidden());
    }

    @Test
    public void registerForTournament_whenCancelledRegistrationExistsForFreeTournament_shouldReactivateRegistration() throws Exception {
        tournament.setEntryFee(BigDecimal.ZERO);
        tournamentRepository.save(tournament);

        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusCancelledAndPaymentStatusRejected(applicant, tournament);
        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(post("/registrations")
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("tournamentId", tournament.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrations/" + registration.getId()));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertEquals(applicant.getId(), result.getPlayer().getId());
        assertEquals(tournament.getId(), result.getTournament().getId());
        assertEquals(PaymentStatus.NOT_REQUIRED, result.getPaymentStatus());
        assertEquals(RegistrationStatus.CONFIRMED, result.getRegistrationStatus());
        assertNotNull(result.getRegisteredOn());
        assertNull(result.getCancelledOn());
        assertNull(result.getPaymentReference());
        assertNull(result.getOrganiserNote());
        assertNull(result.getPaymentSubmittedOn());
        assertNull(result.getReservedUntil());
        assertFalse(result.isHidden());
        assertNotNull(result.getUpdatedOn());
        assertNotNull(result.getRegisteredOn());

    }

    @Test
    public void registerForTournament_whenCancelledRegistrationExistsForPaidTournament_shouldReactivateRegistration() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusCancelledAndPaymentStatusRejected(applicant, tournament);
        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(post("/registrations")
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("tournamentId", tournament.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrations/" + registration.getId()));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertEquals(applicant.getId(), result.getPlayer().getId());
        assertEquals(tournament.getId(), result.getTournament().getId());
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        assertEquals(RegistrationStatus.PENDING_PAYMENT, result.getRegistrationStatus());
        assertNotNull(result.getRegisteredOn());
        assertNull(result.getCancelledOn());
        assertNull(result.getPaymentReference());
        assertNull(result.getOrganiserNote());
        assertNull(result.getPaymentSubmittedOn());
        assertNotNull(result.getReservedUntil());
        assertFalse(result.isHidden());
        assertNotNull(result.getUpdatedOn());
        assertNotNull(result.getRegisteredOn());

    }

    @Test
    public void getRegistrationDetails_whenAuthenticated_shouldReturnRegistrationDetails() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusSubmitted(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(get("/registrations/{id}", registration.getId())
                        .with(user(organiserDetails))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-registration"));
    }


    @Test
    public void cancelRegistration_whenUserIsAdmin_shouldChangeRegistrationStatusToCancelledByAdmin() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusSubmitted(applicant, tournament);

        User unauthorisedUser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(unauthorisedUser);
        AuthenticationUserDetails unauthorisedUserDetails =
                AuthenticationUserDetailsFixture.createFromUser(unauthorisedUser);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(get("/registrations/{id}", registration.getId())
                        .with(user(unauthorisedUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void cancelRegistration_whenUserIsOrganiser_shouldChangeRegistrationStatusToRejected() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusSubmitted(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(patch("/registrations/{id}/cancel", registration.getId())
                        .with(user(organiserDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrations/" + registration.getId()));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn"
                        , "registrationStatus")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
        assertEquals(RegistrationStatus.REJECTED, result.getRegistrationStatus());
    }

    @Test
    public void cancelRegistration_whenUserIsAdmin_shouldChangeRegistrationStatusToRejectedByAdmin() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusSubmitted(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        User admin = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        AuthenticationUserDetails adminDetails = AuthenticationUserDetailsFixture.createFromUser(admin);

        mockMvc.perform(patch("/registrations/{id}/cancel", registration.getId())
                        .with(user(adminDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrations/" + registration.getId()));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn"
                        , "registrationStatus")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
        assertEquals(RegistrationStatus.CANCELLED_BY_ADMIN, result.getRegistrationStatus());
    }

    @Test
    public void cancelRegistration_whenUserIsOwner_shouldChangeRegistrationStatusToCancelled() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusSubmitted(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(patch("/registrations/{id}/cancel", registration.getId())
                        .with(user(applicantDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrations/" + registration.getId()));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn"
                        , "registrationStatus")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
        assertEquals(RegistrationStatus.CANCELLED, result.getRegistrationStatus());
    }

    @Test
    public void hideRegistration_whenUserIsOwner_shouldHideRegistration() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusCancelledAndPaymentStatusRejected(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(delete("/registrations/{id}", registration.getId())
                        .with(user(applicantDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrations"));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn"
                        , "hidden")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
        assertTrue(result.isHidden());
    }

    @Test
    public void addPayment_whenUserIsOwner_shouldChangePaymentStatusToSubmitted() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusPending(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(patch("/registrations/{id}/payment", registration.getId())
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("paymentReference", "TestPaymentReference"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrations/" + registration.getId()));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn"
                        , "paymentStatus"
                        , "paymentReference")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
        assertEquals(PaymentStatus.SUBMITTED, result.getPaymentStatus());
        assertEquals("TestPaymentReference", result.getPaymentReference());
    }

    @Test
    public void addPayment_whenReservedUntilHasExpired_shouldChangeTournamentAndPaymentStatusToExpired() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusPending(applicant, tournament);
        registration.setReservedUntil(LocalDateTime.now().minusMinutes(1));

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(patch("/registrations/{id}/payment", registration.getId())
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("paymentReference", "TestPaymentReference"))
                .andExpect(status().isBadRequest());

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn"
                        , "registrationStatus"
                        , "paymentStatus")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
        assertEquals(PaymentStatus.EXPIRED, result.getPaymentStatus());
        assertEquals(RegistrationStatus.EXPIRED, result.getRegistrationStatus());
    }

    @Test
    public void addPayment_whenPaymentStatusIsConfirmed_shouldThrowBadRequestAndNotChangeRegistration() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusRejectedAndPaymentStatusConfirmed(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(patch("/registrations/{id}/payment", registration.getId())
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("paymentReference", "TestPaymentReference"))
                .andExpect(status().isBadRequest());

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
    }

    @Test
    public void addPayment_whenRegistrationStatusIsPendingPayment_shouldThrowBadRequestAndNotChangeRegistration() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusConfirmed(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(patch("/registrations/{id}/payment", registration.getId())
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("paymentReference", "TestPaymentReference"))
                .andExpect(status().isBadRequest());

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
    }

    @Test
    public void addPayment_whenUserIsNotOwner_shouldThrowBadRequestAndNotChangeRegistration() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusConfirmed(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(patch("/registrations/{id}/payment", registration.getId())
                        .with(user(organiserDetails))
                        .with(csrf())
                        .param("paymentReference", "TestPaymentReference"))
                .andExpect(status().isForbidden());

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
    }

    @Test
    public void addPayment_whenInvalidData_shouldReturnStatusOkAndNotChangeRegistration() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusConfirmed(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(patch("/registrations/{id}/payment", registration.getId())
                        .with(user(applicantDetails))
                        .with(csrf())
                        .param("paymentReference", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-registration"))
                .andExpect(model().attributeHasFieldErrors("paymentRequest", "paymentReference"));


        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(registration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn")
                .isEqualTo(result);
        assertEquals(registration.getTournament().getId(), result.getTournament().getId());
        assertEquals(registration.getPlayer().getId(), result.getPlayer().getId());
    }
    @Test
    public void hideRegistration_whenUserIsNotOwner_shouldReturnForbidden() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusCancelledAndPaymentStatusRejected(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(delete("/registrations/{id}", registration.getId())
                        .with(user(organiserDetails))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(1, tournamentRegistrationRepository.count());

        TournamentRegistration result =
                tournamentRegistrationRepository.findById(registration.getId()).orElseThrow();

        assertEquals(applicant.getId(), result.getPlayer().getId());
        assertEquals(tournament.getId(), result.getTournament().getId());
        assertFalse(result.isHidden());
    }
    @Test
    public void hideRegistration_whenRegistrationIsNotTerminal_shouldReturnBadRequest() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusPending(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(delete("/registrations/{id}", registration.getId())
                        .with(user(applicantDetails))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        assertEquals(1, tournamentRegistrationRepository.count());

        TournamentRegistration result =
                tournamentRegistrationRepository.findById(registration.getId()).orElseThrow();

        assertEquals(applicant.getId(), result.getPlayer().getId());
        assertEquals(tournament.getId(), result.getTournament().getId());
        assertEquals(RegistrationStatus.PENDING_PAYMENT, result.getRegistrationStatus());
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        assertFalse(result.isHidden());
    }

    @Test
    public void cancelRegistration_whenUserIsNotOwnerAdminOrOrganiser_shouldReturnForbidden() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusSubmitted(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        User unauthorizedUser = UserFixture.createWithAllFields();
        unauthorizedUser.setUsername("unauthorized_user");
        unauthorizedUser.setEmail("unauthorized_user@test.com");
        unauthorizedUser.setRole(Role.PLAYER);
        userRepository.save(unauthorizedUser);

        AuthenticationUserDetails unauthorizedUserDetails =
                AuthenticationUserDetailsFixture.createFromUser(unauthorizedUser);

        mockMvc.perform(patch("/registrations/{id}/cancel", registration.getId())
                        .with(user(unauthorizedUserDetails))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(1, tournamentRegistrationRepository.count());

        TournamentRegistration result =
                tournamentRegistrationRepository.findById(registration.getId()).orElseThrow();

        assertEquals(applicant.getId(), result.getPlayer().getId());
        assertEquals(tournament.getId(), result.getTournament().getId());
        assertEquals(RegistrationStatus.PENDING_PAYMENT, result.getRegistrationStatus());
        assertEquals(PaymentStatus.SUBMITTED, result.getPaymentStatus());
        assertNull(result.getCancelledOn());
    }

    @Test
    public void cancelRegistration_whenRegistrationAlreadyTerminal_shouldReturnBadRequest() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusCancelledAndPaymentStatusRejected(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(patch("/registrations/{id}/cancel", registration.getId())
                        .with(user(applicantDetails))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        assertEquals(1, tournamentRegistrationRepository.count());

        TournamentRegistration result =
                tournamentRegistrationRepository.findById(registration.getId()).orElseThrow();

        assertEquals(applicant.getId(), result.getPlayer().getId());
        assertEquals(tournament.getId(), result.getTournament().getId());
        assertEquals(RegistrationStatus.CANCELLED, result.getRegistrationStatus());
        assertEquals(PaymentStatus.REJECTED, result.getPaymentStatus());
    }

    @Test
    public void getRegistrationDetails_whenOwner_shouldReturnRegistrationDetails() throws Exception {
        TournamentRegistration registration = TournamentRegistrationFixture
                .createWithRegistrationStatusPendingAndPaymentStatusSubmitted(applicant, tournament);

        tournamentRegistrationRepository.save(registration);

        mockMvc.perform(get("/registrations/{id}", registration.getId())
                        .with(user(applicantDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-registration"))
                .andExpect(model().attributeExists("registration"))
                .andExpect(model().attributeExists("paymentRequest"));
    }
}

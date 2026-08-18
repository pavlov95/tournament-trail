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
import tournament_trail.demo.fixtures.dtos.TournamentRequestFixture;
import tournament_trail.demo.repositories.TournamentRegistrationRepository;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.repositories.UserRepository;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.web.dtos.TournamentRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    private AuthenticationUserDetails userDetails;
    private AuthenticationUserDetails organiserDetails;
    private User organiser;
    private User user;

    @BeforeEach
    public void setUp() {
        tournamentRegistrationRepository.deleteAll();
        tournamentRepository.deleteAll();
        userRepository.deleteAll();

        user = UserFixture.createWithAllFieldsAndRoleOrganiser();
        organiser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        organiser.setRole(Role.ORGANISER);
        userRepository.save(user);
        userRepository.save(organiser);
        userDetails = AuthenticationUserDetailsFixture.createFromUser(user);
        organiserDetails = AuthenticationUserDetailsFixture.createFromUser(organiser);
    }

    @Test
    public void getTournament_shouldReturnSearchedTournaments() throws Exception {
        mockMvc.perform(get("/tournaments"))
                .andExpect(status().isOk())
                .andExpect(view().name("tournaments"));

    }

    @Test
    public void getCreateTournamentPage_whenUserIsOrganiser_shouldReturnCreateTournamentPage() throws Exception {
        mockMvc.perform(get("/tournaments/create")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-create"));
    }

    @Test
    public void getCreateTournamentPage_whenUserIsNotOrganiser_shouldReturnForbidden() throws Exception {
        User nonOrganiser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(nonOrganiser);

        AuthenticationUserDetails nonOrganiserDetails =
                AuthenticationUserDetailsFixture.createFromUser(nonOrganiser);

        mockMvc.perform(get("/tournaments/create")
                        .with(user(nonOrganiserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void createTournament_whenAuthenticatedAndValidInput_shouldCreateTournament() throws Exception {
        TournamentRequest tournamentRequest = TournamentRequestFixture.create();

        mockMvc.perform(post("/tournaments/create")
                        .with(user(userDetails))
                        .with(csrf())
                        .flashAttr("tournamentRequest", tournamentRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/tournaments/*"));

        assertEquals(1, tournamentRepository.count());

        Tournament tournament = tournamentRepository.findAll().get(0);

        assertThat(tournament)
                .usingRecursiveComparison()
                .ignoringFields("id"
                        , "createdOn"
                        , "updatedOn"
                        , "startTime"
                        , "registrationDeadline"
                        , "endTime"
                        , "entryFee"
                        , "organiser"
                        , "status")
                .isEqualTo(tournamentRequest);
    }

    @Test
    public void createTournament_whenInvalidData_shouldReturnCreatePageAndNotCreateTournament() throws Exception {
        TournamentRequest tournamentRequest = TournamentRequestFixture.createInvalid();

        mockMvc.perform(post("/tournaments/create")
                        .with(user(userDetails))
                        .with(csrf())
                        .flashAttr("tournamentRequest", tournamentRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-create"))
                .andExpect(model().attributeHasFieldErrors("tournamentRequest"));

        assertEquals(0, tournamentRepository.count());
    }

    @Test
    public void editTournament_whenValidData_shouldEditTournament() throws Exception {
        Tournament tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        TournamentRequest tournamentRequest = TournamentRequestFixture.create();

        mockMvc.perform(put("/tournaments/{tournamentId}", tournament.getId())
                        .with(user(organiserDetails))
                        .with(csrf())
                        .flashAttr("tournamentRequest", tournamentRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tournaments/" + tournament.getId()));

        assertEquals(1, tournamentRepository.count());

        Tournament result = tournamentRepository.findAll().get(0);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id"
                        , "createdOn"
                        , "updatedOn"
                        , "startTime"
                        , "registrationDeadline"
                        , "endTime"
                        , "entryFee"
                        , "organiser"
                        , "status")
                .isEqualTo(tournamentRequest);
    }

    @Test
    public void editTournament_whenInvalidData_shouldNotEditTournament() throws Exception {
        Tournament tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        TournamentRequest tournamentRequest = TournamentRequestFixture.createInvalid();

        mockMvc.perform(put("/tournaments/{tournamentId}", tournament.getId())
                        .with(user(organiserDetails))
                        .with(csrf())
                        .flashAttr("tournamentRequest", tournamentRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-details"))
                .andExpect(model().hasErrors());

        assertEquals(1, tournamentRepository.count());

        Tournament result = tournamentRepository.findAll().get(0);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createdOn"
                        , "updatedOn"
                        , "startTime"
                        , "registrationDeadline"
                        , "endTime"
                        , "entryFee"
                        , "organiser")
                .isEqualTo(tournament);
        assertEquals(tournament.getOrganiser().getId(), result.getOrganiser().getId());
    }

    @Test
    public void getTournament_shouldShowTournamentDetails() throws Exception {
        Tournament tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        mockMvc.perform(get("/tournaments/{tournamentId}", tournament.getId()))
//                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-details"));

    }

    @Test
    public void updateTournamentStatus_whenUserIsOrganiser_shouldChangeStatus() throws Exception {
        Tournament tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        mockMvc.perform(patch("/tournaments/{tournamentId}", tournament.getId())
                        .with(user(organiserDetails))
                        .with(csrf())
                        .param("status", TournamentStatus.CANCELLED.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tournaments/" + tournament.getId()));

        assertEquals(1, tournamentRepository.count());

        Tournament result = tournamentRepository.findAll().get(0);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createdOn"
                        , "updatedOn"
                        , "startTime"
                        , "registrationDeadline"
                        , "endTime"
                        , "entryFee"
                        , "organiser"
                        , "status")
                .isEqualTo(tournament);

        assertNotEquals(tournament.getStatus(), result.getStatus());
        assertEquals(TournamentStatus.CANCELLED, result.getStatus());
        assertEquals(tournament.getOrganiser().getId(), result.getOrganiser().getId());
    }


    @Test
    public void getRegistrationsForTournament_whenAuthenticated_shouldShowRegistrations() throws Exception {
        Tournament tournament = TournamentFixture.createWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        mockMvc.perform(get("/tournaments/{tournamentId}/registrations", tournament.getId())
                        .with(user(organiserDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-registration-management"));
    }

    @Test
    public void approveRegistration_whenUserIsOrganiser_shouldSetStatusToConfirmed() throws Exception {
        Tournament tournament = TournamentFixture.createWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        TournamentRegistration tournamentRegistration =
                TournamentRegistrationFixture.createWithRegistrationStatusPendingAndPaymentStatusSubmitted(user, tournament);
        tournamentRegistrationRepository.save(tournamentRegistration);

        mockMvc.perform(patch("/tournaments/{tournamentId}/registrations/{registrationId}/approve-payment"
                        , tournament.getId(), tournamentRegistration.getId())
                        .with(user(organiserDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tournaments/" + tournament.getId() + "/registrations"));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertEquals(PaymentStatus.CONFIRMED, result.getPaymentStatus());
        assertEquals(RegistrationStatus.CONFIRMED, result.getRegistrationStatus());
        assertEquals("Your payment has been confirmed. We look forward to seeing you at "
                        + tournament.getName() + "."
                , result.getOrganiserNote());
    }

    @Test
    public void rejectRegistration_whenUserIsOrganiserWithValidInput_shouldSetStatusToRejectedWithDefaultRejectMessage() throws Exception {
        Tournament tournament = TournamentFixture.createWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        TournamentRegistration tournamentRegistration =
                TournamentRegistrationFixture.createWithRegistrationStatusPendingAndPaymentStatusSubmitted(user, tournament);
        tournamentRegistrationRepository.save(tournamentRegistration);

        mockMvc.perform(patch("/tournaments/{tournamentId}/registrations/{registrationId}/reject-payment"
                        , tournament.getId(), tournamentRegistration.getId())
                        .with(user(organiserDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tournaments/" + tournament.getId() + "/registrations"));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertEquals(PaymentStatus.REJECTED, result.getPaymentStatus());
        assertEquals(RegistrationStatus.REJECTED, result.getRegistrationStatus());
        assertEquals("Your registration has been declined by the organiser."
                , result.getOrganiserNote());
    }

    @Test
    public void rejectRegistration_whenAuthenticatedWithValidInput_shouldSetStatusToRejected() throws Exception {
        Tournament tournament = TournamentFixture.createWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        TournamentRegistration tournamentRegistration =
                TournamentRegistrationFixture.createWithRegistrationStatusPendingAndPaymentStatusSubmitted(user, tournament);
        tournamentRegistrationRepository.save(tournamentRegistration);

        mockMvc.perform(patch("/tournaments/{tournamentId}/registrations/{registrationId}/reject-payment"
                        , tournament.getId(), tournamentRegistration.getId())
                        .with(user(organiserDetails))
                        .with(csrf())
                        .param("organiserNote", "Test OrganiserNote"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tournaments/" + tournament.getId() + "/registrations"));

        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertEquals(PaymentStatus.REJECTED, result.getPaymentStatus());
        assertEquals(RegistrationStatus.REJECTED, result.getRegistrationStatus());
        assertEquals("Test OrganiserNote", result.getOrganiserNote());
    }

    @Test
    public void rejectRegistration_whenAuthenticatedWithInvalidInput_shouldNotChangeStatus() throws Exception {
        Tournament tournament = TournamentFixture.createWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        TournamentRegistration tournamentRegistration =
                TournamentRegistrationFixture.createWithRegistrationStatusPendingAndPaymentStatusSubmitted(user, tournament);
        tournamentRegistrationRepository.save(tournamentRegistration);

        mockMvc.perform(patch("/tournaments/{tournamentId}/registrations/{registrationId}/reject-payment"
                        , tournament.getId(), tournamentRegistration.getId())
                        .with(user(organiserDetails))
                        .with(csrf())
                        .param("organiserNote", "a".repeat(501)))
                .andExpect(status().isOk())
                .andExpect(view().name("tournament-registration-management"))
                .andExpect(model().attributeHasFieldErrors("organiserNoteRequest", "organiserNote"));
        assertEquals(1, tournamentRegistrationRepository.count());
        TournamentRegistration result = tournamentRegistrationRepository.findAll().get(0);

        assertThat(tournamentRegistration)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "player"
                        , "registeredOn"
                        , "updatedOn"
                        , "cancelledOn"
                        , "reservedUntil"
                        , "paymentSubmittedOn")
                .isEqualTo(result);
    }
    @Test
    public void editTournament_whenDifferentOrganiser_shouldReturnForbidden() throws Exception {
        Tournament tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        String originalName = tournament.getName();
        TournamentStatus originalStatus = tournament.getStatus();

        User differentOrganiser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        differentOrganiser.setRole(Role.ORGANISER);
        userRepository.save(differentOrganiser);

        AuthenticationUserDetails differentOrganiserDetails =
                AuthenticationUserDetailsFixture.createFromUser(differentOrganiser);

        TournamentRequest tournamentRequest = TournamentRequestFixture.create();

        mockMvc.perform(put("/tournaments/{tournamentId}", tournament.getId())
                        .with(user(differentOrganiserDetails))
                        .with(csrf())
                        .flashAttr("tournamentRequest", tournamentRequest))
                .andExpect(status().isForbidden());

        assertEquals(1, tournamentRepository.count());

        Tournament result = tournamentRepository.findById(tournament.getId()).orElseThrow();

        assertEquals(originalName, result.getName());
        assertEquals(originalStatus, result.getStatus());
        assertEquals(organiser.getId(), result.getOrganiser().getId());
    }

    @Test
    public void updateTournamentStatus_whenDifferentOrganiser_shouldReturnForbidden() throws Exception {
        Tournament tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        TournamentStatus originalStatus = tournament.getStatus();

        User differentOrganiser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        differentOrganiser.setRole(Role.ORGANISER);
        userRepository.save(differentOrganiser);

        AuthenticationUserDetails differentOrganiserDetails =
                AuthenticationUserDetailsFixture.createFromUser(differentOrganiser);

        mockMvc.perform(patch("/tournaments/{tournamentId}", tournament.getId())
                        .with(user(differentOrganiserDetails))
                        .with(csrf())
                        .param("status", TournamentStatus.CANCELLED.name()))
                .andExpect(status().isForbidden());

        assertEquals(1, tournamentRepository.count());

        Tournament result = tournamentRepository.findById(tournament.getId()).orElseThrow();

        assertEquals(originalStatus, result.getStatus());
        assertEquals(organiser.getId(), result.getOrganiser().getId());
    }

    @Test
    public void getRegistrationsForTournament_whenDifferentOrganiser_shouldReturnForbidden() throws Exception {
        Tournament tournament = TournamentFixture.createWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        User differentOrganiser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        differentOrganiser.setRole(Role.ORGANISER);
        userRepository.save(differentOrganiser);

        AuthenticationUserDetails differentOrganiserDetails =
                AuthenticationUserDetailsFixture.createFromUser(differentOrganiser);

        mockMvc.perform(get("/tournaments/{tournamentId}/registrations", tournament.getId())
                        .with(user(differentOrganiserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void autocompleteTournaments_whenQueryProvided_shouldReturnTournamentOptions() throws Exception {
        Tournament tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournament.setName("Varna Chess Open");
        tournamentRepository.save(tournament);

        mockMvc.perform(get("/tournaments/autocomplete")
                        .param("query", "Varna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tournament.getId().toString()))
                .andExpect(jsonPath("$[0].label").value("Varna Chess Open"));
    }
}

package tournament_trail.demo.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tournament_trail.demo.entities.*;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import tournament_trail.demo.entities.enums.TravelRequestStatus;
import tournament_trail.demo.fixtures.*;
import tournament_trail.demo.fixtures.dtos.TravelGroupRequestFixture;
import tournament_trail.demo.repositories.*;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.web.dtos.TravelGroupRequest;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TravelGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TravelGroupRepository travelGroupRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TravelRequestRepository travelRequestRepository;

    @Autowired
    private TravelGroupCommentRepository travelGroupCommentRepository;

    @Autowired
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private AuthenticationUserDetails userDetails;
    private Tournament tournament;
    private TravelGroup travelGroup;

    @BeforeEach
    public void setUp() {
        travelGroupCommentRepository.deleteAllInBatch();
        travelRequestRepository.deleteAllInBatch();
        tournamentRegistrationRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        travelGroupRepository.deleteAllInBatch();
        tournamentRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();


        User user = UserFixture.createWithAllFields();
        userRepository.save(user);
        userDetails = AuthenticationUserDetailsFixture.createFromUser(user);
        tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(user);
        tournamentRepository.save(tournament);

        travelGroup = TravelGroupFixture.createWithoutIdAndUserAndTournament();
        travelGroup.setOwner(user);
        travelGroup.setTournament(tournament);
        travelGroupRepository.save(travelGroup);
    }


    @Test
    public void getTravelGroups_whenAuthenticated_shouldReturnTravelGroupsPage() throws Exception {
        mockMvc.perform(get("/travel-groups")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("travel-groups"));
    }

    @Test
    public void getTravelGroups_whenNotAuthenticated_shouldRedirect() throws Exception {
        mockMvc.perform(get("/travel-groups"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    public void getCreateTravelGroupPage_whenAuthenticated_shouldReturnCreateTravelGroupPage() throws Exception {
        mockMvc.perform(get("/travel-groups/create")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("travel-group-create"));
    }

    @Test
    public void createTravelGroup_whenValidData_shouldCreateTravelGroup() throws Exception {
        travelGroupRepository.deleteAll();
        TravelGroupRequest travelGroupRequest = TravelGroupRequestFixture.create();
        travelGroupRequest.setTournamentId(tournament.getId());

        mockMvc.perform(post("/travel-groups/create")
                        .with(user(userDetails))
                        .with(csrf())
                        .flashAttr(("travelGroupRequest"), travelGroupRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/travel-groups/*"));

        assertEquals(1, travelGroupRepository.count());
        TravelGroup result = travelGroupRepository.findAll().get(0);
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("tournament"
                        , "id"
                        , "owner"
                        , "createdOn"
                        , "updatedOn"
                        , "status"
                        , "estimatedCost"
                        , "departureTime")
                .isEqualTo(travelGroupRequest);
    }

    @Test
    public void createTravelGroup_whenInvalidData_shouldNotCreateTravelGroup() throws Exception {
        travelGroupRepository.deleteAll();
        TravelGroupRequest travelGroupRequest = TravelGroupRequestFixture.createInvalid();
        travelGroupRequest.setTournamentId(tournament.getId());

        mockMvc.perform(post("/travel-groups/create")
                        .with(user(userDetails))
                        .with(csrf())
                        .flashAttr(("travelGroupRequest"), travelGroupRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("travel-group-create"))
                .andExpect(model().attributeHasFieldErrors("travelGroupRequest"));

        assertEquals(0, travelGroupRepository.count());
    }

    @Test
    public void getTravelGroup_whenAuthenticated_shouldShowTravelGroup() throws Exception {

        mockMvc.perform(get("/travel-groups/{id}", travelGroup.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("travel-group-details"))
                .andExpect(model().attributeExists("travelGroup"))
                .andExpect(model().attributeExists("travelGroupRequest"))
                .andExpect(model().attributeExists("travelJoinRequest"))
                .andExpect(model().attributeExists("isOwner"))
                .andExpect(model().attributeExists("availableSpots"))
                .andExpect(model().attributeExists("canSendJoinRequest"));
    }

    @Test
    public void cancelTravelGroup_whenAuthenticated_shouldCancelTravelGroup() throws Exception {

        mockMvc.perform(patch("/travel-groups/{id}", travelGroup.getId())
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups"));

        assertEquals(1, travelGroupRepository.count());
        TravelGroup result = travelGroupRepository.findAll().get(0);

        assertEquals(TravelGroupStatus.CANCELLED, result.getStatus());
    }

    @Test
    public void cancelTravelGroup_whenUserIsNotOwner_shouldReturnForbidden() throws Exception {

        User nonOwner = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(nonOwner);
        AuthenticationUserDetails nonOwnerDetails =
                AuthenticationUserDetailsFixture.createFromUser(nonOwner);

        mockMvc.perform(patch("/travel-groups/{id}", travelGroup.getId())
                        .with(user(nonOwnerDetails))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(1, travelGroupRepository.count());
        TravelGroup result = travelGroupRepository.findAll().get(0);

        assertNotEquals(TravelGroupStatus.CANCELLED, result.getStatus());
    }

    @Test
    public void updateTravelGroup_whenValidData_shouldUpdateTravelGroup() throws Exception {
        TravelGroupRequest travelGroupRequest = TravelGroupRequestFixture.create();
        travelGroupRequest.setTournamentId(tournament.getId());

        mockMvc.perform(put("/travel-groups/{id}", travelGroup.getId())
                        .with(user(userDetails))
                        .with(csrf())
                        .flashAttr("travelGroupRequest", travelGroupRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups/" + travelGroup.getId()));

        assertEquals(1, travelGroupRepository.count());
        TravelGroup result = travelGroupRepository.findAll().get(0);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields(
                        "tournament"
                        , "id"
                        , "owner"
                        , "createdOn"
                        , "updatedOn"
                        , "status"
                        , "estimatedCost"
                        , "departureTime")
                .isEqualTo(travelGroupRequest);
    }


    @Test
    public void updateTravelGroup_whenNotOwner_shouldReturnForbidden() throws Exception {
        TravelGroupRequest travelGroupRequest = TravelGroupRequestFixture.create();
        travelGroupRequest.setTournamentId(tournament.getId());

        User nonOwner = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(nonOwner);
        AuthenticationUserDetails nonOwnerDetails =
                AuthenticationUserDetailsFixture.createFromUser(nonOwner);

        mockMvc.perform(put("/travel-groups/{id}", travelGroup.getId())
                        .with(user(nonOwnerDetails))
                        .with(csrf())
                        .flashAttr("travelGroupRequest", travelGroupRequest))
                .andExpect(status().isForbidden());

        assertEquals(1, travelGroupRepository.count());
        TravelGroup result = travelGroupRepository.findAll().get(0);
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields(
                        "createdOn"
                        , "updatedOn"
                        , "tournament"
                        , "owner"
                        , "estimatedCost"
                        ,"departureTime")
                .isEqualTo(travelGroup);


    }

    @Test
    public void sendTravelGroupRequest_whenValidData_shouldSendTravelGroupRequest() throws Exception {
        User applicant = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(applicant);
        AuthenticationUserDetails participantDetails =
                AuthenticationUserDetailsFixture.createFromUser(applicant);

        mockMvc.perform(post("/travel-groups/{id}/send-request", travelGroup.getId())
                        .with(user(participantDetails))
                        .with(csrf())
                        .param("message", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups/" + travelGroup.getId()));

        assertEquals(1, travelRequestRepository.count());
        TravelRequest result = travelRequestRepository.findAll().get(0);

        assertEquals("Test", result.getMessage());
        assertEquals(travelGroup.getId(), result.getTravelGroup().getId());
        assertEquals(TravelRequestStatus.PENDING, result.getStatus());
        assertEquals(applicant.getId(), result.getApplicant().getId());
    }


    @Test
    public void sendTravelGroupRequest_whenInvalidData_shouldNotCreateRequest() throws Exception {
        User applicant = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(applicant);
        AuthenticationUserDetails participantDetails =
                AuthenticationUserDetailsFixture.createFromUser(applicant);

        mockMvc.perform(post("/travel-groups/{id}/send-request", travelGroup.getId())
                        .with(user(participantDetails))
                        .with(csrf())
                        .param("message", "a".repeat(501)))
                .andExpect(status().isOk())
                .andExpect(view().name("travel-group-details"))
                .andExpect(model().attributeHasFieldErrors("travelJoinRequest", "message"));

        assertEquals(0, travelRequestRepository.count());
    }

    @Test
    public void sendTravelGroupRequest_whenOwnerTriesToJoinOwnGroup_shouldNotCreateRequest() throws Exception {

        mockMvc.perform(post("/travel-groups/{id}/send-request", travelGroup.getId())
                        .with(user(userDetails))
                        .with(csrf())
                        .param("message", "Test"))
                .andExpect(status().isBadRequest());

        assertEquals(0, travelRequestRepository.count());
    }


    @Test
    public void sendTravelGroupRequest_whenAlreadyRequested_shouldNotCreateDuplicateRequest() throws Exception {
        User applicant = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(applicant);

        AuthenticationUserDetails participantDetails =
                AuthenticationUserDetailsFixture.createFromUser(applicant);

        mockMvc.perform(post("/travel-groups/{id}/send-request", travelGroup.getId())
                        .with(user(participantDetails))
                        .with(csrf())
                        .param("message", "First request"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups/" + travelGroup.getId()));

        assertEquals(1, travelRequestRepository.count());

        mockMvc.perform(post("/travel-groups/{id}/send-request", travelGroup.getId())
                        .with(user(participantDetails))
                        .with(csrf())
                        .param("message", "Second request"))
                .andExpect(status().is4xxClientError());

        assertEquals(1, travelRequestRepository.count());
    }

    @Test
    public void sendTravelGroupRequest_whenGroupIsCancelled_shouldNotCreateRequest() throws Exception {
        travelGroup.setStatus(TravelGroupStatus.CANCELLED);
        travelGroupRepository.save(travelGroup);

        User applicant = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(applicant);

        AuthenticationUserDetails participantDetails =
                AuthenticationUserDetailsFixture.createFromUser(applicant);

        mockMvc.perform(post("/travel-groups/{id}/send-request", travelGroup.getId())
                        .with(user(participantDetails))
                        .with(csrf())
                        .param("message", "Test"))
                .andExpect(status().isForbidden());

        assertEquals(0, travelRequestRepository.count());
    }

    @Test
    public void updateTravelGroup_whenInvalidData_shouldReturnDetailsPageAndNotUpdate() throws Exception {
        TravelGroupRequest invalidRequest = TravelGroupRequestFixture.createInvalid();
        invalidRequest.setTournamentId(tournament.getId());

        mockMvc.perform(put("/travel-groups/{id}", travelGroup.getId())
                        .with(user(userDetails))
                        .with(csrf())
                        .flashAttr("travelGroupRequest", invalidRequest))
                .andExpect(status().isOk())
                .andExpect(view().name("travel-group-details"))
                .andExpect(model().attributeHasFieldErrors("travelGroupRequest"));

        assertEquals(1, travelGroupRepository.count());
    }
}
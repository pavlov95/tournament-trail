package tournament_trail.demo.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import tournament_trail.demo.entities.enums.TravelRequestStatus;
import tournament_trail.demo.fixtures.*;
import tournament_trail.demo.repositories.*;
import tournament_trail.demo.security.AuthenticationUserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TravelRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TravelRequestRepository travelRequestRepository;

    @Autowired
    private TravelGroupRepository travelGroupRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TravelGroupCommentRepository travelGroupCommentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    private AuthenticationUserDetails ownerDetails;
    private AuthenticationUserDetails organiserDetails;
    private User applicant;
    private AuthenticationUserDetails applicantDetails;
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

        User owner = UserFixture.createWithAllFields();
        User organiser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        applicant = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(owner);
        userRepository.save(organiser);
        userRepository.save(applicant);

        ownerDetails = AuthenticationUserDetailsFixture.createFromUser(owner);
        organiserDetails = AuthenticationUserDetailsFixture.createFromUser(organiser);
        applicantDetails = AuthenticationUserDetailsFixture.createFromUser(applicant);

        Tournament tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        travelGroup = TravelGroupFixture.createWithoutIdAndUserAndTournament();
        travelGroup.setOwner(owner);
        travelGroup.setTournament(tournament);
        travelGroupRepository.save(travelGroup);

    }

    @Test
    public void getRequests_whenAuthenticated_shouldReturnRequests() throws Exception {
        mockMvc.perform(get("/travel-groups/{travelGroupId}/requests", travelGroup.getId())
                        .with(user(ownerDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("travel-group-requests"));
    }

    @Test
    public void getRequests_whenNotOwner_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/travel-groups/{travelGroupId}/requests", travelGroup.getId())
                        .with(user(organiserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void acceptRequest_whenAuthenticated_shouldChangeStatusAndRedirect() throws Exception {
        TravelRequest travelRequest =
                TravelRequestFixture.createWithPendingStatus(applicant, travelGroup);
        travelRequestRepository.save(travelRequest);

        mockMvc.perform(patch("/travel-groups/{travelGroupId}/requests/{travelRequestId}/accept"
                        , travelGroup.getId(), travelRequest.getId())
                        .with(user(ownerDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups/" + travelGroup.getId() + "/requests"));

        assertEquals(1, travelRequestRepository.count());

        TravelRequest result = travelRequestRepository.findAll().get(0);

        assertEquals(travelRequest.getId(), result.getId());
        assertEquals(TravelRequestStatus.APPROVED , result.getStatus());
        assertEquals(travelRequest.getTravelGroup().getId(), result.getTravelGroup().getId());
        assertEquals(travelRequest.getApplicant().getId(), result.getApplicant().getId());
    }

    @Test
    public void acceptRequest_whenNotOwner_shouldReturnForbidden() throws Exception {
        TravelRequest travelRequest =
                TravelRequestFixture.createWithPendingStatus(applicant, travelGroup);
        travelRequestRepository.save(travelRequest);

        mockMvc.perform(patch("/travel-groups/{travelGroupId}/requests/{travelRequestId}/accept"
                        , travelGroup.getId(), travelRequest.getId())
                        .with(user(applicantDetails))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(1, travelRequestRepository.count());

        TravelRequest result = travelRequestRepository.findAll().get(0);

        assertEquals(TravelRequestStatus.PENDING , result.getStatus());
    }

    @Test
    public void acceptRequest_whenTravelGroupIsNotOpen_shouldNotChangeStatusAndReturnBadRequest() throws Exception {
        TravelRequest travelRequest =
                TravelRequestFixture.createWithPendingStatus(applicant, travelGroup);
        travelRequestRepository.save(travelRequest);
        travelGroup.setStatus(TravelGroupStatus.CLOSED);
        travelGroupRepository.save(travelGroup);

        mockMvc.perform(patch("/travel-groups/{travelGroupId}/requests/{travelRequestId}/accept"
                        , travelGroup.getId(), travelRequest.getId())
                        .with(user(ownerDetails))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        assertEquals(1, travelRequestRepository.count());

        TravelRequest result = travelRequestRepository.findAll().get(0);

        assertEquals(TravelRequestStatus.PENDING , result.getStatus());
    }

    @Test
    public void acceptRequest_whenTravelRequestStatusIsNotPending_shouldNotChangeStatusAndReturnBadRequest() throws Exception {
        TravelRequest travelRequest =
                TravelRequestFixture.createWithApprovedStatus(applicant, travelGroup);
        travelRequestRepository.save(travelRequest);

        mockMvc.perform(patch("/travel-groups/{travelGroupId}/requests/{travelRequestId}/accept"
                        , travelGroup.getId(), travelRequest.getId())
                        .with(user(ownerDetails))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        assertEquals(1, travelRequestRepository.count());

        TravelRequest result = travelRequestRepository.findAll().get(0);

        assertEquals(TravelRequestStatus.APPROVED , result.getStatus());
    }

    @Test
    public void acceptRequest_whenTravelGroupIsFull_shouldNotChangeStatusAndReturnBadRequest() throws Exception {
        travelGroup.setMaximumMembers(2); // owner + 1 approved member = full
        travelGroupRepository.save(travelGroup);

        User approvedApplicant = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        userRepository.save(approvedApplicant);

        TravelRequest approvedRequest =
                TravelRequestFixture.createWithApprovedStatus(approvedApplicant, travelGroup);
        travelRequestRepository.save(approvedRequest);

        TravelRequest pendingRequest =
                TravelRequestFixture.createWithPendingStatus(applicant, travelGroup);
        travelRequestRepository.save(pendingRequest);

        mockMvc.perform(patch("/travel-groups/{travelGroupId}/requests/{travelRequestId}/accept",
                        travelGroup.getId(), pendingRequest.getId())
                        .with(user(ownerDetails))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        assertEquals(2, travelRequestRepository.count());

        TravelRequest result = travelRequestRepository.findById(pendingRequest.getId()).orElseThrow();

        assertEquals(TravelRequestStatus.PENDING, result.getStatus());
    }


    @Test
    public void rejectRequest_whenAuthenticated_shouldRejectStatus() throws Exception {
        TravelRequest travelRequest =
                TravelRequestFixture.createWithPendingStatus(applicant, travelGroup);
        travelRequestRepository.save(travelRequest);

        mockMvc.perform(patch("/travel-groups/{travelGroupId}/requests/{travelRequestId}/reject"
                        , travelGroup.getId(), travelRequest.getId())
                        .with(user(ownerDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups/" + travelGroup.getId() + "/requests"));

        assertEquals(1, travelRequestRepository.count());

        TravelRequest result = travelRequestRepository.findAll().get(0);

        assertEquals(travelRequest.getId(), result.getId());
        assertEquals(TravelRequestStatus.REJECTED , result.getStatus());
        assertEquals(travelRequest.getTravelGroup().getId(), result.getTravelGroup().getId());
        assertEquals(travelRequest.getApplicant().getId(), result.getApplicant().getId());
    }

    @Test
    public void rejectRequest_whenNotOwner_shouldNotChangeStatusAndReturnForbidden() throws Exception {
        TravelRequest travelRequest =
                TravelRequestFixture.createWithPendingStatus(applicant, travelGroup);
        travelRequestRepository.save(travelRequest);

        mockMvc.perform(patch("/travel-groups/{travelGroupId}/requests/{travelRequestId}/reject"
                        , travelGroup.getId(), travelRequest.getId())
                        .with(user(applicantDetails))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(1, travelRequestRepository.count());

        TravelRequest result = travelRequestRepository.findAll().get(0);

        assertEquals(travelRequest.getId(), result.getId());
        assertEquals(TravelRequestStatus.PENDING , result.getStatus());
        assertEquals(travelRequest.getTravelGroup().getId(), result.getTravelGroup().getId());
        assertEquals(travelRequest.getApplicant().getId(), result.getApplicant().getId());
    }

    @Test
    public void rejectRequest_whenTravelRequestStatusIsNotPending_shouldNotChangeStatus() throws Exception {
        TravelRequest travelRequest =
                TravelRequestFixture.createWithApprovedStatus(applicant, travelGroup);
        travelRequestRepository.save(travelRequest);

        mockMvc.perform(patch("/travel-groups/{travelGroupId}/requests/{travelRequestId}/reject"
                        , travelGroup.getId(), travelRequest.getId())
                        .with(user(ownerDetails))
                        .with(csrf()))
                .andExpect(status().isForbidden());
        assertEquals(1, travelRequestRepository.count());

        TravelRequest result = travelRequestRepository.findAll().get(0);

        assertEquals(travelRequest.getId(), result.getId());
        assertEquals(TravelRequestStatus.APPROVED , result.getStatus());
        assertEquals(travelRequest.getTravelGroup().getId(), result.getTravelGroup().getId());
        assertEquals(travelRequest.getApplicant().getId(), result.getApplicant().getId());
    }
}

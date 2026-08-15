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
import tournament_trail.demo.entities.TravelGroupComment;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.fixtures.*;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.repositories.TravelGroupCommentRepository;
import tournament_trail.demo.repositories.TravelGroupRepository;
import tournament_trail.demo.repositories.UserRepository;
import tournament_trail.demo.security.AuthenticationUserDetails;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TravelGroupRepository travelGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TravelGroupCommentRepository commentRepository;

    private User user;
    private AuthenticationUserDetails userDetails;
    private Tournament tournament;
    private TravelGroup travelGroup;
    private TravelGroupComment comment;

    @BeforeEach
    public void setUp() {
        commentRepository.deleteAll();
        travelGroupRepository.deleteAll();
        tournamentRepository.deleteAll();
        userRepository.deleteAll();

        user = UserFixture.createWithAllFields();
        userRepository.save(user);

        userDetails = AuthenticationUserDetailsFixture.createFromUser(user);

        tournament = TournamentFixture.createWithoutIdAndOrganiser();
        tournament.setOrganiser(user);
        tournamentRepository.save(tournament);

        travelGroup = TravelGroupFixture.createWithoutIdAndUserAndTournament();
        travelGroup.setOwner(user);
        travelGroup.setTournament(tournament);
        travelGroupRepository.save(travelGroup);

        comment = TravelGroupCommentFixture.createWithAuthorAndTravelGroup(user, travelGroup);
        commentRepository.save(comment);
    }

    @Test
    public void getCommentsPage_whenNotAuthenticated_shouldRedirect() throws Exception {
        mockMvc.perform(get("/travel-groups/{travelGroupId}/comments", UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

    }

    @Test
    public void getCommentsPage_whenAuthenticated_shouldShowComments() throws Exception {
        mockMvc.perform(get("/travel-groups/{travelGroupId}/comments", travelGroup.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("comment"));
    }

    @Test
    public void createComment_whenValidData_shouldCreateComment() throws Exception {
        mockMvc.perform(post("/travel-groups/{travelGroupId}/comments", travelGroup.getId())
                        .param("content", "Test")
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertEquals(2, commentRepository.count());
    }

    @Test
    public void createComment_whenInvalidData_shouldReturnStatusOkAndFieldError() throws Exception {
        mockMvc.perform(post("/travel-groups/{travelGroupId}/comments", travelGroup.getId())
                        .param("content", "")
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("comment"))
                .andExpect(model().attributeHasFieldErrors("commentRequest", "content"));

        assertEquals(1, commentRepository.count());
    }

    @Test
    public void editComment_whenValidData_shouldEditComment() throws Exception {
        mockMvc.perform(patch("/travel-groups/{travelGroupId}/comments/{commentId}/edit"
                        , travelGroup.getId(), comment.getId())
                        .param("content", "Test")
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups/" + travelGroup.getId() + "/comments"));

        assertEquals(1, commentRepository.count());
        TravelGroupComment result = commentRepository.findById(comment.getId()).get();
        assertEquals("Test", result.getContent());
    }

    @Test
    public void editComment_whenInvalidData_shouldNotEditCommentAndReturnStatusOk() throws Exception {
        mockMvc.perform(patch("/travel-groups/{travelGroupId}/comments/{commentId}/edit"
                        , travelGroup.getId(), comment.getId())
                        .param("content", "")
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("comment"))
                .andExpect(model().attributeHasFieldErrors("commentRequest", "content"));

        assertEquals(1, commentRepository.count());
        TravelGroupComment result = commentRepository.findById(comment.getId()).get();
        assertNotEquals("", result.getContent());
    }

    @Test
    public void pinComment_whenAuthenticated_shouldPinComment() throws Exception {
        mockMvc.perform(patch("/travel-groups/{travelGroupId}/comments/{commentId}/pin"
                        , travelGroup.getId(), comment.getId())
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups/" + travelGroup.getId() + "/comments"));

        assertEquals(1, commentRepository.count());
        TravelGroupComment result = commentRepository.findById(comment.getId()).get();
        assertTrue(result.isPinned());

    }

    @Test
    public void unpinComment_whenAuthenticated_shouldUnpinComment() throws Exception {
        mockMvc.perform(patch("/travel-groups/{travelGroupId}/comments/{commentId}/unpin"
                        , travelGroup.getId(), comment.getId())
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups/" + travelGroup.getId() + "/comments"));

        assertEquals(1, commentRepository.count());
        TravelGroupComment result = commentRepository.findById(comment.getId()).get();
        assertFalse(result.isPinned());

    }

    @Test
    public void deleteComment_whenAuthenticated_shouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/travel-groups/{travelGroupId}/comments/{commentId}"
                        , travelGroup.getId(), comment.getId())
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/travel-groups/" + travelGroup.getId() + "/comments"));

        assertEquals(1, commentRepository.count());
        TravelGroupComment result = commentRepository.findById(comment.getId()).get();
        assertTrue(result.isHidden());
    }
}

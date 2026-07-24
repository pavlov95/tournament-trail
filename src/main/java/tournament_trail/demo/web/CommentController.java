package tournament_trail.demo.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import tournament_trail.demo.entities.TravelGroupComment;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.TravelGroupCommentService;
import tournament_trail.demo.web.dtos.CommentRequest;
import tournament_trail.demo.web.dtos.TravelGroupCommentsPageData;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/travel-groups/{travelGroupId}/comments")
public class CommentController {
    private final TravelGroupCommentService travelGroupCommentService;

    public CommentController(TravelGroupCommentService travelGroupCommentService) {
        this.travelGroupCommentService = travelGroupCommentService;
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ModelAndView getCommentsPage(@PathVariable UUID travelGroupId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        ModelAndView modelAndView = new ModelAndView("comment");
        addCommonData(modelAndView, travelGroupId, userDetails.getId());
        modelAndView.addObject("commentRequest", new CommentRequest());
        return modelAndView;
    }

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ModelAndView createComment(@PathVariable UUID travelGroupId
            , @Valid @ModelAttribute CommentRequest commentRequest, BindingResult bindingResult
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("comment");

            addCommonData(modelAndView, travelGroupId, userDetails.getId());
            modelAndView.addObject("commentRequest", commentRequest);

            return modelAndView;
        }

        travelGroupCommentService.createComment(travelGroupId, commentRequest.getContent(), userDetails.getId());
        return new ModelAndView("redirect:/travel-groups/" + travelGroupId + "/comments");
    }

    @PatchMapping("/{commentId}/edit")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView editComment(@PathVariable UUID travelGroupId, @PathVariable UUID commentId
            , @Valid @ModelAttribute CommentRequest commentRequest, BindingResult bindingResult
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("comment");

            addCommonData(modelAndView, travelGroupId, userDetails.getId());
            modelAndView.addObject("commentRequest", commentRequest);

            return modelAndView;
        }
        travelGroupCommentService.editComment(travelGroupId, userDetails.getId(), commentId, commentRequest.getContent());

        return new ModelAndView("redirect:/travel-groups/" + travelGroupId + "/comments");
    }

    @PatchMapping("/{commentId}/pin")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView pinComment(@PathVariable UUID travelGroupId, @PathVariable UUID commentId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        travelGroupCommentService.pinComment(travelGroupId, userDetails.getId(), commentId);

        return new ModelAndView("redirect:/travel-groups/" + travelGroupId + "/comments");
    }

    @PatchMapping("/{commentId}/unpin")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView unpinComment(@PathVariable UUID travelGroupId, @PathVariable UUID commentId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails){

        travelGroupCommentService.unpinComment(travelGroupId, userDetails.getId(), commentId);

        return new ModelAndView("redirect:/travel-groups/" + travelGroupId + "/comments");
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView deleteComment(@PathVariable UUID travelGroupId, @PathVariable UUID commentId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        travelGroupCommentService.deleteComment(travelGroupId, userDetails.getId(), commentId);

        return new ModelAndView("redirect:/travel-groups/" + travelGroupId + "/comments");
    }

    private void addCommonData(ModelAndView modelAndView, UUID travelGroupId, UUID userId){
        TravelGroupCommentsPageData commentsPageData = travelGroupCommentService.getCommentsPageData(
                travelGroupId, userId);

        modelAndView.addObject("travelGroup", commentsPageData.travelGroup());
        modelAndView.addObject("countVisibleComments", commentsPageData.countVisibleComments());
        modelAndView.addObject("comments", commentsPageData.comments());
    }
}

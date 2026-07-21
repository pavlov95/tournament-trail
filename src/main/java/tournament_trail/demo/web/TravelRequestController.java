package tournament_trail.demo.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.enums.TravelRequestStatus;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.TravelGroupService;
import tournament_trail.demo.services.TravelRequestService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/travel-groups/{travelGroupId}/requests")
public class TravelRequestController {
    private final TravelGroupService travelGroupService;
    private final TravelRequestService travelRequestService;

    public TravelRequestController(TravelGroupService travelGroupService, TravelRequestService travelRequestService) {
        this.travelGroupService = travelGroupService;
        this.travelRequestService = travelRequestService;
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ModelAndView getRequests(@PathVariable UUID travelGroupId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        TravelGroup travelGroup = travelGroupService.findById(travelGroupId);

        if (!travelGroup.getOwner().getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("You are not allowed to view these requests.");
        }

        ModelAndView modelAndView = new ModelAndView("travel-group-requests");

        modelAndView.addObject("travelGroup", travelGroup);
        addCommonData(modelAndView, travelGroupId, travelRequestService.countAvailableSpots(travelGroup));

        return modelAndView;
    }

    @PatchMapping("/{travelRequestId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView acceptRequest(@PathVariable UUID travelGroupId, @PathVariable UUID travelRequestId,
                                      @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        TravelGroup travelGroup = travelGroupService.findById(travelGroupId);
        int availableSpots = travelRequestService.countAvailableSpots(travelGroup);

        travelRequestService.acceptTravelRequest(travelGroupId, travelRequestId, userDetails.getId(), availableSpots);

        return new ModelAndView("redirect:/travel-groups/" + travelGroupId + "/requests");
    }

    @PatchMapping("/{travelRequestId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView rejectRequest(@PathVariable UUID travelGroupId, @PathVariable UUID travelRequestId
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        travelRequestService.rejectTravelRequest(travelGroupId, travelRequestId, userDetails.getId());

        return new ModelAndView("redirect:/travel-groups/" + travelGroupId + "/requests");
    }

    private void addCommonData(ModelAndView modelAndView, UUID travelGroupId, int availableSpots) {
        List<TravelRequest> travelRequests = travelRequestService.getAllPendingRequests(
                travelGroupId, TravelRequestStatus.PENDING);

        modelAndView.addObject("travelRequests", travelRequests);
        modelAndView.addObject("availableSpots", availableSpots);
    }
}

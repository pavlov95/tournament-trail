package tournament_trail.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TransportationType;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import tournament_trail.demo.services.TravelGroupService;
import tournament_trail.demo.services.TravelRequestService;
import tournament_trail.demo.web.dtos.TravelGroupSearchRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.security.AuthenticationUserDetails;
import tournament_trail.demo.services.TournamentService;
import tournament_trail.demo.web.dtos.TravelGroupRequest;
import tournament_trail.demo.web.dtos.TravelJoinRequest;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/travel-groups")
public class TravelGroupController {

    private final TravelGroupService travelGroupService;
    private final TournamentService tournamentService;
    private final TravelRequestService travelRequestService;

    public TravelGroupController(TravelGroupService travelGroupService, TournamentService tournamentService
            , TravelRequestService travelRequestService) {
        this.travelGroupService = travelGroupService;
        this.tournamentService = tournamentService;
        this.travelRequestService = travelRequestService;
    }

    @GetMapping
    public ModelAndView getTravelGroups(@ModelAttribute("searchRequest") TravelGroupSearchRequest searchRequest) {
        ModelAndView modelAndView = new ModelAndView("travel-groups");

        List<TravelGroup> travelGroups = travelGroupService.searchTravelGroups(searchRequest);
        modelAndView.addObject("travelGroups", travelGroups);

        modelAndView.addObject("searchRequest", searchRequest);

        return modelAndView;
    }

    @GetMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView getCreateTravelGroupPage(@RequestParam(value = "tournamentId", required = false)
                                                 UUID tournamentId) {
        ModelAndView modelAndView = new ModelAndView("travel-group-create");

        TravelGroupRequest travelGroupRequest = new TravelGroupRequest();

        travelGroupRequest.setTournamentId(tournamentId);
        addCommonData(modelAndView);
        modelAndView.addObject("travelGroupRequest", travelGroupRequest);

        modelAndView.addObject("selectedTournamentLabel",
                tournamentService.getTournamentOptionLabel(tournamentId));

        return modelAndView;
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView createTravelGroup(@Valid @ModelAttribute("travelGroupRequest")
                                          TravelGroupRequest travelGroupRequest, BindingResult bindingResult,
                                          @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("travel-group-create");
            modelAndView.addObject("selectedTournamentLabel",
                    tournamentService.getTournamentOptionLabel(travelGroupRequest.getTournamentId()));
            addCommonData(modelAndView);
            return modelAndView;
        }

        TravelGroup travelGroup = travelGroupService.createTravelGroup(travelGroupRequest,
                userDetails.getId());

        return new ModelAndView("redirect:/travel-groups/" + travelGroup.getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView getTravelGroup(@PathVariable UUID id
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        ModelAndView modelAndView = new ModelAndView("travel-group-details");

        TravelGroup travelGroup = travelGroupService.findById(id);
        addTravelGroupDetailsData(modelAndView, travelGroup, userDetails
                , travelGroupService.mapToTravelGroupRequest(travelGroup), new TravelJoinRequest());
        return modelAndView;
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView cancelTravelGroup(@PathVariable UUID id
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails) {

        travelGroupService.cancel(id, userDetails.getId(), userDetails.getRole());

        return new ModelAndView("redirect:/travel-groups");
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView updateTravelGroup(@PathVariable UUID id
            , @Valid @ModelAttribute("travelGroupRequest") TravelGroupRequest travelGroupRequest
            , BindingResult bindingResult, @AuthenticationPrincipal AuthenticationUserDetails userDetails) {


        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("travel-group-details");
            TravelGroup travelGroup = travelGroupService.findById(id);

            addTravelGroupDetailsData(modelAndView, travelGroup, userDetails, travelGroupRequest
                    , new TravelJoinRequest());
            return modelAndView;
        }
        travelGroupService.updateTravelGroup(id, travelGroupRequest, userDetails.getId(), userDetails.getRole());
        return new ModelAndView("redirect:/travel-groups/" + id);
    }

    @PostMapping("/{id}/send-request")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView sendTravelGroupRequest(@PathVariable UUID id
            , @Valid @ModelAttribute("travelJoinRequest") TravelJoinRequest travelJoinRequest, BindingResult bindingResult
            , @AuthenticationPrincipal AuthenticationUserDetails userDetails, RedirectAttributes redirectAttributes) {

        TravelGroup travelGroup = travelGroupService.findById(id);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("travel-group-details");

            addTravelGroupDetailsData(modelAndView, travelGroup, userDetails
                    , travelGroupService.mapToTravelGroupRequest(travelGroup), travelJoinRequest);

            return modelAndView;
        }
        travelRequestService.createTravelRequest(travelGroup, userDetails.getId(), travelJoinRequest.getMessage());
        redirectAttributes.addFlashAttribute("successMessage", "Request sent successfully");

        return new ModelAndView("redirect:/travel-groups/" + id);
    }

    private void addCommonData(ModelAndView modelAndView) {
        modelAndView.addObject("currencies", CurrencyCode.values());
        modelAndView.addObject("transportationTypes", TransportationType.values());
    }

    private void addTravelGroupDetailsData(ModelAndView modelAndView, TravelGroup travelGroup
            , AuthenticationUserDetails userDetails, TravelGroupRequest travelGroupRequest
            , TravelJoinRequest travelJoinRequest) {

        boolean isOwner = travelGroup.getOwner().getId().equals(userDetails.getId());
        boolean isApprovedMember = travelRequestService.isApprovedMember(travelGroup.getId(), userDetails.getId());

        boolean canViewPrivateGroupArea = isOwner || isApprovedMember;

        List<TravelRequest> approvedApplicants = travelRequestService.getApprovedApplicantsForTravelGroup(
                travelGroup.getId());

        int availableSpots = travelRequestService.countAvailableSpots(travelGroup);
        boolean hasAlreadyRequested = travelRequestService.hasRequestFromUser(
                travelGroup.getId(),
                userDetails.getId()
        );

        boolean canSendJoinRequest = !isOwner
                        && availableSpots > 0
                        && !hasAlreadyRequested
                        && travelGroup.getStatus() == TravelGroupStatus.OPEN;

        modelAndView.addObject("travelGroup", travelGroup);
        modelAndView.addObject("travelGroupRequest", travelGroupRequest);
        modelAndView.addObject("travelJoinRequest", travelJoinRequest);
        modelAndView.addObject("isOwner", isOwner);
        modelAndView.addObject("availableSpots", Math.max(availableSpots, 0));
        modelAndView.addObject("canSendJoinRequest", canSendJoinRequest);
        modelAndView.addObject("approvedApplicants", approvedApplicants);
        modelAndView.addObject("isApprovedMember", isApprovedMember);
        modelAndView.addObject("canViewPrivateGroupArea", canViewPrivateGroupArea);

        addCommonData(modelAndView);
    }
}


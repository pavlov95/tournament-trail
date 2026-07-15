package tournament_trail.demo.web;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TransportationType;
import tournament_trail.demo.services.TravelGroupService;
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

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/travel-groups")
public class TravelGroupController {

    private final TravelGroupService travelGroupService;
    private final TournamentService tournamentService;

    public TravelGroupController(TravelGroupService travelGroupService,
                                 TournamentService tournamentService) {
        this.travelGroupService = travelGroupService;
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public ModelAndView getTravelGroups(@ModelAttribute("searchRequest")
            TravelGroupSearchRequest searchRequest) {
        ModelAndView modelAndView = new ModelAndView("travel-groups");

        List<TravelGroup> travelGroups = travelGroupService.searchTravelGroups(searchRequest);
        modelAndView.addObject("travelGroups", travelGroups);

        modelAndView.addObject("searchRequest", searchRequest);

        return modelAndView;
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('TRAVEL_GROUP_CREATE')")
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
    @PreAuthorize("hasAuthority('TRAVEL_GROUP_CREATE')")
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
    public ModelAndView getTravelGroup(@PathVariable UUID id) {
        TravelGroup travelGroup = travelGroupService.findById(id);

        ModelAndView modelAndView = new ModelAndView("travel-group-details");

        modelAndView.addObject("travelGroup", travelGroup);
        modelAndView.addObject("travelGroupRequest",
                travelGroupService.mapToTravelGroupRequest(travelGroup));
        addCommonData(modelAndView);

        return modelAndView;
    }

    @PatchMapping("/{id}")
    public ModelAndView cancelTravelGroup(@PathVariable UUID id,
             @AuthenticationPrincipal AuthenticationUserDetails userDetails){

        travelGroupService.cancel(id, userDetails.getId(), userDetails.getRole());

        return new ModelAndView("redirect:/travel-groups");
    }

    @PutMapping("/{id}")
    public ModelAndView updateTravelGroup(@PathVariable UUID id, @Valid TravelGroupRequest travelGroupRequest
            , BindingResult bindingResult, @AuthenticationPrincipal AuthenticationUserDetails userDetails){
            ModelAndView modelAndView = new ModelAndView("travel-group-details");
            if(bindingResult.hasErrors()){
                modelAndView.addObject("travelGroup", travelGroupService.findById(id));
                addCommonData(modelAndView);
                return modelAndView;
            }
            travelGroupService.updateTravelGroup(id, travelGroupRequest, userDetails.getId(), userDetails.getRole());
            return new ModelAndView("redirect:/travel-groups/" + id);
    }

    private void addCommonData(ModelAndView modelAndView){
        modelAndView.addObject("currencies",CurrencyCode.values());
        modelAndView.addObject("transportationTypes", TransportationType.values());
    }
}


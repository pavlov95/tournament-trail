package tournament_trail.demo.services;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import tournament_trail.demo.exceptions.TravelGroupDoesNotExistException;
import tournament_trail.demo.repositories.TravelGroupRepository;
import tournament_trail.demo.web.dtos.TravelGroupRequest;
import tournament_trail.demo.web.dtos.TravelGroupSearchRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TravelGroupService {

    private final TravelGroupRepository travelGroupRepository;
    private final TournamentService tournamentService;
    private final UserService userService;

    public TravelGroupService(TravelGroupRepository travelGroupRepository,
                              TournamentService tournamentService, UserService userService) {
        this.travelGroupRepository = travelGroupRepository;
        this.tournamentService = tournamentService;
        this.userService = userService;
    }

    public List<TravelGroup> searchTravelGroups(TravelGroupSearchRequest searchRequest) {
        Specification<TravelGroup> specification = Specification.where(null);

        specification = specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("status"), TravelGroupStatus.OPEN));

        if (searchRequest.getName() != null && !searchRequest.getName().isBlank()) {

            String name = searchRequest.getName().trim().toLowerCase();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                                    "%" + name + "%"));
        }

        if (searchRequest.getDepartureCountry() != null
                && !searchRequest.getDepartureCountry().isBlank()) {

            String departureCountry = searchRequest.getDepartureCountry().trim().toLowerCase();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(criteriaBuilder.lower(
                                    root.get("departureCountry")), departureCountry));
        }

        if (searchRequest.getDepartureCity() != null && !searchRequest.getDepartureCity().isBlank()) {

            String departureCity = searchRequest.getDepartureCity().trim().toLowerCase();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(criteriaBuilder.lower(
                                    root.get("departureCity")), departureCity));
        }

        return travelGroupRepository.findAll(specification,
                Sort.by(Sort.Direction.ASC, "departureTime"));
    }

    public TravelGroup findById(UUID id) {
        return travelGroupRepository.findById(id).orElseThrow(TravelGroupDoesNotExistException::new);
    }

    @Transactional
    public TravelGroup createTravelGroup(TravelGroupRequest request, UUID ownerId) {
        User owner = userService.findById(ownerId);

        Tournament tournament = tournamentService.findById(request.getTournamentId());

        validateTournamentForTravelGroupCreation(tournament);
        validateDepartureTime(request, tournament);

        TravelGroup travelGroup = TravelGroup.builder()
                .name(request.getName())
                .departureCountry(request.getDepartureCountry())
                .departureCity(request.getDepartureCity())
                .departureTime(request.getDepartureTime())
                .maximumMembers(request.getMaximumMembers())
                .description(request.getDescription())
                .status(TravelGroupStatus.OPEN)
                .tournament(tournament)
                .meetingPoint(request.getMeetingPoint())
                .transportationType(request.getTransportationType())
                .estimatedCost(request.getEstimatedCost())
                .currency(request.getCurrency())
                .owner(owner)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        return travelGroupRepository.save(travelGroup);
    }

    private void validateTournamentForTravelGroupCreation(Tournament tournament) {
        if (tournament.getStatus() != TournamentStatus.PUBLISHED) {
            throw new IllegalStateException("Travel groups can only be created for published tournaments.");
        }

        if (!tournament.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Cannot create a travel group for a tournament that has already started.");
        }
    }

    private void validateDepartureTime(TravelGroupRequest request, Tournament tournament) {
        if (!request.getDepartureTime().isBefore(tournament.getStartTime())) {
            throw new IllegalStateException("Departure time must be before the tournament starts.");
        }
    }

    public List<TravelGroup> getTravelGroupsByUser(UUID userId) {
        return travelGroupRepository.findAllByOwnerIdOrderByDepartureTimeAsc(userId);
    }

    @Transactional
    public void cancel(UUID travelGroupId, UUID userId, Role role) {
        TravelGroup travelGroup = findById(travelGroupId);
        UUID ownerId = travelGroup.getOwner().getId();
        boolean isOwner = ownerId.equals(userId);
        boolean isAdmin = role.equals(Role.ADMIN);
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to alter this travel group");
        }
        if (travelGroup.getStatus() == TravelGroupStatus.CANCELLED) {
            throw new IllegalStateException("Travel group is already cancelled.");
        }
        travelGroup.setStatus(TravelGroupStatus.CANCELLED);
        travelGroup.setUpdatedOn(LocalDateTime.now());
        travelGroupRepository.save(travelGroup);
    }

    public TravelGroupRequest mapToTravelGroupRequest(TravelGroup travelGroup) {

        TravelGroupRequest travelGroupRequest = new TravelGroupRequest();

        travelGroupRequest.setName(travelGroup.getName());
        travelGroupRequest.setDepartureCountry(travelGroup.getDepartureCountry());
        travelGroupRequest.setDepartureCity(travelGroup.getDepartureCity());
        travelGroupRequest.setDepartureTime(travelGroup.getDepartureTime());
        travelGroupRequest.setMaximumMembers(travelGroup.getMaximumMembers());
        travelGroupRequest.setDescription(travelGroup.getDescription());
        travelGroupRequest.setTournamentId(travelGroup.getTournament().getId());
        travelGroupRequest.setMeetingPoint(travelGroup.getMeetingPoint());
        travelGroupRequest.setTransportationType(travelGroup.getTransportationType());
        travelGroupRequest.setEstimatedCost(travelGroup.getEstimatedCost());
        travelGroupRequest.setCurrency(travelGroup.getCurrency());

        return travelGroupRequest;
    }

    @Transactional
    public void updateTravelGroup(UUID id, TravelGroupRequest travelGroupRequest, UUID userId, Role role) {
        TravelGroup travelGroup = findById(id);

        boolean isOwner = userId.equals(travelGroup.getOwner().getId());
        boolean isAdmin = role == Role.ADMIN;

        if(!isOwner && !isAdmin){
            throw new AccessDeniedException("You are not authorised to alter this travel group");
        }

        travelGroup.setName(travelGroupRequest.getName());
        travelGroup.setDepartureCountry(travelGroupRequest.getDepartureCountry());
        travelGroup.setDepartureCity(travelGroupRequest.getDepartureCity());
        travelGroup.setDepartureTime(travelGroupRequest.getDepartureTime());
        travelGroup.setMaximumMembers(travelGroupRequest.getMaximumMembers());
        travelGroup.setDescription(travelGroupRequest.getDescription());
        travelGroup.setTournament(tournamentService.findById(travelGroupRequest.getTournamentId()));
        travelGroup.setMeetingPoint(travelGroupRequest.getMeetingPoint());
        travelGroup.setTransportationType(travelGroupRequest.getTransportationType());
        travelGroup.setEstimatedCost(travelGroupRequest.getEstimatedCost());
        travelGroup.setCurrency(travelGroupRequest.getCurrency());
        travelGroup.setUpdatedOn(LocalDateTime.now());

        travelGroupRepository.save(travelGroup);
    }
}
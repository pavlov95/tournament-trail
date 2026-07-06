package tournament_trail.demo.services;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.User;
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
}
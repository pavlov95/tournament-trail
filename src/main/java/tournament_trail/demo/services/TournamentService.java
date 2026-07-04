package tournament_trail.demo.services;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.exceptions.InvalidTournamentTimeCriteria;
import tournament_trail.demo.exceptions.TournamentDoesNotExist;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.web.dtos.TournamentRequest;
import tournament_trail.demo.web.dtos.TournamentSearchRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final UserService userService;

    public TournamentService(TournamentRepository tournamentRepository, UserService userService) {
        this.tournamentRepository = tournamentRepository;
        this.userService = userService;
    }

    public List<Tournament> getAllUpcomingTournaments() {
        return tournamentRepository.findAllByOrderByRegistrationDeadlineAsc();
    }

    @Transactional
    public Tournament createTournament(TournamentRequest tournamentRequest, UUID userID) {
        verifyTournamentTime(tournamentRequest);
        User organiser = userService.findById(userID);

        Tournament tournament = Tournament.builder()
                .name(tournamentRequest.getName())
                .venue(tournamentRequest.getVenue())
                .city(tournamentRequest.getCity())
                .country(tournamentRequest.getCountry())
                .currency(tournamentRequest.getCurrency())
                .registrationDeadline(tournamentRequest.getRegistrationDeadline())
                .startTime(tournamentRequest.getStartTime())
                .endTime(tournamentRequest.getEndTime())
                .maximumParticipants(tournamentRequest.getMaximumParticipants())
                .entryFee(tournamentRequest.getEntryFee())
                .paymentInstructions(tournamentRequest.getPaymentInstructions())
                .participationRequirements(tournamentRequest.getParticipationRequirements())
                .description(tournamentRequest.getDescription())
                .edition(tournamentRequest.getEdition())
                .timeControl(tournamentRequest.getTimeControl())
                .rated(tournamentRequest.isRated())
                .status(TournamentStatus.DRAFT)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .organiser(organiser)
                .build();
        return tournamentRepository.save(tournament);
    }

    public List<Tournament> searchTournaments(TournamentSearchRequest request) {
        Specification<Tournament> specification = Specification.where(null);

        specification = specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("status"), TournamentStatus.PUBLISHED));

        // Show only tournaments that have not started.
        specification = specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), LocalDateTime.now()));

        if (request.getName() != null && !request.getName().isBlank()) {
            String name = request.getName()
                    .trim()
                    .toLowerCase();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.like(criteriaBuilder.lower(
                                    root.get("name")), "%" + name + "%"));
        }

        if (request.getCountry() != null && !request.getCountry().isBlank()) {

            String country = request.getCountry()
                    .trim()
                    .toLowerCase();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(criteriaBuilder.lower(
                                    root.get("country")), country));
        }

        if (request.getCity() != null && !request.getCity().isBlank()) {

            String city = request.getCity()
                    .trim()
                    .toLowerCase();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(criteriaBuilder.lower(root.get("city")), city));
        }

        if (request.getTimeControl() != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("timeControl"), request.getTimeControl()));
        }

        if (request.getRated() != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("rated"), request.getRated()));
        }

        return tournamentRepository.findAll(specification,
                Sort.by(Sort.Direction.ASC, "startTime"));
    }

    private void verifyTournamentTime(TournamentRequest tournamentRequest) {
        LocalDateTime startTime = tournamentRequest.getStartTime();
        LocalDateTime endTime = tournamentRequest.getEndTime();
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new InvalidTournamentTimeCriteria();
        }
    }

    public Tournament findById(UUID id) {
        return tournamentRepository.findById(id).orElseThrow(TournamentDoesNotExist::new);
    }


    public void cancelTournament(UUID tournamentId, UUID userId, Role currentUserRole) {
        Tournament tournament = findById(tournamentId);
        boolean isAdmin = currentUserRole == Role.ADMIN;
        boolean owner = tournament.getOrganiser()
                .getId()
                .equals(userId);

        if (!isAdmin && !owner) {
            throw new AccessDeniedException("You cannot cancel this tournament.");
        }

        if (tournament.getStatus() == TournamentStatus.COMPLETED) {
            throw new IllegalStateException("A completed tournament cannot be cancelled.");
        }

        if (tournament.getStatus() == TournamentStatus.CANCELLED) {
            throw new IllegalStateException("The tournament is already cancelled.");
        }

        tournament.setStatus(TournamentStatus.CANCELLED);
        tournament.setUpdatedOn(LocalDateTime.now());
        tournamentRepository.save(tournament);
    }

    public void editTournament(TournamentRequest tournamentRequest, UUID tournamentId,
                               UUID userId, Role role) {
        verifyTournamentTime(tournamentRequest);
        Tournament tournament = findById(tournamentId);

        boolean isOwner = tournament.getOrganiser().getId().equals(userId);
        boolean isAdmin = role == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to alter this tournament");
        }
        if (tournament.getStatus() == TournamentStatus.CANCELLED
                || tournament.getStatus() == TournamentStatus.COMPLETED) {
            throw new IllegalStateException("Cancelled or completed tournaments cannot be edited.");
        }

        tournament.setName((tournamentRequest.getName()));
        tournament.setVenue(tournamentRequest.getVenue());
        tournament.setCity(tournamentRequest.getCity());
        tournament.setCountry(tournamentRequest.getCountry());
        tournament.setCurrency(tournamentRequest.getCurrency());
        tournament.setRegistrationDeadline(tournamentRequest.getRegistrationDeadline());
        tournament.setStartTime(tournamentRequest.getStartTime());
        tournament.setEndTime(tournamentRequest.getEndTime());
        tournament.setMaximumParticipants(tournamentRequest.getMaximumParticipants());
        tournament.setEntryFee(tournamentRequest.getEntryFee());
        tournament.setPaymentInstructions(tournamentRequest.getPaymentInstructions());
        tournament.setParticipationRequirements(tournamentRequest.getParticipationRequirements());
        tournament.setDescription(tournamentRequest.getDescription());
        tournament.setEdition(tournamentRequest.getEdition());
        tournament.setTimeControl(tournamentRequest.getTimeControl());
        tournament.setRated(tournamentRequest.isRated());
        tournament.setUpdatedOn(LocalDateTime.now());

        tournamentRepository.save(tournament);

    }

    public TournamentRequest mapToTournamentRequest(Tournament tournament) {
        TournamentRequest request = new TournamentRequest();

        request.setName(tournament.getName());
        request.setVenue(tournament.getVenue());
        request.setRegistrationDeadline(tournament.getRegistrationDeadline());
        request.setTimeControl(tournament.getTimeControl());
        request.setCountry(tournament.getCountry());
        request.setCity(tournament.getCity());
        request.setEntryFee(tournament.getEntryFee());
        request.setCurrency(tournament.getCurrency());
        request.setStartTime(tournament.getStartTime());
        request.setEndTime(tournament.getEndTime());
        request.setEdition(tournament.getEdition());
        request.setDescription(tournament.getDescription());
        request.setRated(tournament.isRated());
        request.setMaximumParticipants(tournament.getMaximumParticipants());
        request.setParticipationRequirements(tournament.getParticipationRequirements());
        request.setPaymentInstructions(tournament.getPaymentInstructions());

        return request;
    }

    public boolean canEditTournament(
            Tournament tournament,
            UUID userId,
            Role role
    ) {
        boolean isAdmin = role == Role.ADMIN;

        boolean isOwner = tournament.getOrganiser()
                .getId()
                .equals(userId);

        return isAdmin || isOwner;
    }
}
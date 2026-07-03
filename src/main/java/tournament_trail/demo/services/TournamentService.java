package tournament_trail.demo.services;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.exceptions.InvalidTournamentTimeCriteria;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.web.dtos.CreateTournamentRequest;
import tournament_trail.demo.web.dtos.TournamentSearchRequest;

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
    public void createTournament(CreateTournamentRequest createTournamentRequest, UUID userID) {
        verifyTournamentTime(createTournamentRequest);
        User organiser = userService.findById(userID);

        Tournament tournament = Tournament.builder()
                .name(createTournamentRequest.getName())
                .venue(createTournamentRequest.getVenue())
                .city(createTournamentRequest.getCity())
                .country(createTournamentRequest.getCountry())
                .currency(createTournamentRequest.getCurrency())
                .registrationDeadline(createTournamentRequest.getRegistrationDeadline())
                .startTime(createTournamentRequest.getStartTime())
                .endTime(createTournamentRequest.getEndTime())
                .maximumParticipants(createTournamentRequest.getMaximumParticipants())
                .entryFee(createTournamentRequest.getEntryFee())
                .paymentInstructions(createTournamentRequest.getPaymentInstructions())
                .participationRequirements(createTournamentRequest.getParticipationRequirements())
                .description(createTournamentRequest.getDescription())
                .edition(createTournamentRequest.getEdition())
                .timeControl(createTournamentRequest.getTimeControl())
                .rated(createTournamentRequest.isRated())
                .status(TournamentStatus.DRAFT)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .organiser(organiser)
                .build();
        tournamentRepository.save(tournament);
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

    private void verifyTournamentTime(CreateTournamentRequest createTournamentRequest){
        LocalDateTime startTime = createTournamentRequest.getStartTime();
        LocalDateTime endTime = createTournamentRequest.getEndTime();
        if(startTime.isAfter(endTime) || startTime.isEqual(endTime)){
            throw new InvalidTournamentTimeCriteria();
        }
    }
}

package tournament_trail.demo.config.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import tournament_trail.demo.entities.*;
import tournament_trail.demo.entities.enums.*;
import tournament_trail.demo.resultclient.GameRequest;
import tournament_trail.demo.resultclient.GameResult;
import tournament_trail.demo.resultclient.WinCondition;
import tournament_trail.demo.services.*;
import tournament_trail.demo.web.dtos.ReviewRequest;
import tournament_trail.demo.web.dtos.TravelGroupRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Profile("demo")
public class DemoDataInitializer {

    @Bean
    ApplicationRunner seedDemoData(TournamentService tournamentService, UserService userService
            , TournamentRegistrationService tournamentRegistrationService, TravelGroupService travelGroupService
            , PasswordEncoder passwordEncoder, TravelRequestService travelRequestService, ReviewService reviewService
            , TournamentResultsService tournamentResultsService
            , @Value("${demo.organiser.password:}") String demoPassword) {

        return args -> {

            if (demoPassword == null || demoPassword.isBlank()) {
                throw new IllegalStateException("DEMO_ORGANISER_PASSWORD must be configured");
            }

            User organiser = userService
                    .findByUsername("demo_organiser")
                    .orElseGet(() -> {
                        LocalDateTime now = LocalDateTime.now();

                        User newOrganiser = User.builder()
                                .username("demo_organiser")
                                .firstName("Demo")
                                .lastName("Organiser")
                                .email("demo-organiser@example.invalid")
                                .password(passwordEncoder.encode(demoPassword))
                                .role(Role.ORGANISER)
                                .enabled(true)
                                .createdOn(now)
                                .updatedOn(now)
                                .build();

                        return userService.save(newOrganiser);
                    });

            String tournamentName = "Varna Open 2027";

            if (!tournamentService.existsByName(tournamentName)) {

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start = now.plusMonths(3);

                Tournament tournament = Tournament.builder()
                        .name(tournamentName)
                        .venue("Palace of Culture and Sports")
                        .city("Varna")
                        .country("Bulgaria")
                        .startTime(start)
                        .endTime(start.plusDays(2))
                        .registrationDeadline(start.minusDays(7))
                        .timeControl(TimeControl.CLASSICAL)
                        .currency(CurrencyCode.EUR)
                        .entryFee(new BigDecimal("20.00"))
                        .edition(1)
                        .description("Demo chess tournament")
                        .rated(true)
                        .maximumParticipants(100)
                        .participationRequirements("Open to all players")
                        .paymentInstructions("Demo event - no payment required")
                        .status(TournamentStatus.PUBLISHED)
                        .organiser(organiser)
                        .createdOn(now)
                        .updatedOn(now)
                        .build();

                tournamentService.save(tournament);

                System.out.println("Demo tournament created: " + tournament.getId());
            }

            createDemoPlayers(userService, passwordEncoder, demoPassword);
            createDemoTournaments(tournamentService, organiser);
            createDemoRegistrations(tournamentService, userService, tournamentRegistrationService);
            createDemoTravelGroups(tournamentService, userService, travelGroupService);
            createDemoTravelRequests(userService, travelGroupService, travelRequestService);
            createDemoReviews(tournamentService, tournamentRegistrationService, reviewService);

            createDemoGames(tournamentService, tournamentRegistrationService, tournamentResultsService);

            System.out.println("Demo data initialization completed!");
        };
    }

    private void createDemoPlayers(UserService userService, PasswordEncoder passwordEncoder, String demoPassword) {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 20; i++) {

            String username = String.format("demo_player_%02d", i);

            if (userService.findByUsername(username).isPresent()) {
                continue;
            }

            User player = User.builder()
                    .username(username)
                    .firstName("Demo")
                    .lastName("Player " + i)
                    .email(username + "@example.invalid")
                    .password(passwordEncoder.encode(demoPassword))
                    .role(Role.PLAYER)
                    .enabled(true)
                    .createdOn(now)
                    .updatedOn(now)
                    .build();

            userService.save(player);
        }


    }

    private void createDemoTournaments(TournamentService tournamentService, User organiser) {

        LocalDateTime now = LocalDateTime.now();

        TournamentStatus[] statuses = {
                TournamentStatus.PUBLISHED,
                TournamentStatus.PUBLISHED,
                TournamentStatus.PUBLISHED,
                TournamentStatus.REGISTRATION_CLOSED,
                TournamentStatus.DRAFT,
                TournamentStatus.CANCELLED,
                TournamentStatus.STARTED,
                TournamentStatus.COMPLETED
        };

        TimeControl[] timeControls = {
                TimeControl.CLASSICAL,
                TimeControl.RAPID,
                TimeControl.BLITZ,
                TimeControl.BULLET
        };

        String[] cities = {"Varna", "Sofia", "Plovdiv", "Burgas", "Ruse", "Pleven"};

        for (int i = 1; i <= 29; i++) {

            String name = String.format("Demo Chess Tournament %02d", i);

            if (tournamentService.existsByName(name)) {
                continue;
            }

            TournamentStatus status = statuses[(i - 1) % statuses.length];
            TimeControl timeControl = timeControls[(i - 1) % timeControls.length];
            String city = cities[(i - 1) % cities.length];

            LocalDateTime start;

            if (status == TournamentStatus.COMPLETED) {
                start = now.minusDays(20 + i);
            } else if (status == TournamentStatus.STARTED) {
                start = now.minusHours(2);
            } else {
                start = now.plusDays(20 + i * 3L);
            }

            LocalDateTime end = status == TournamentStatus.STARTED ? now.plusDays(35) : start.plusDays(2);
            LocalDateTime deadline = start.minusDays(7);

            Tournament tournament = Tournament.builder()
                    .name(name)
                    .venue("Demo Chess Hall")
                    .city(city)
                    .country("Bulgaria")
                    .startTime(start)
                    .endTime(end)
                    .registrationDeadline(deadline)
                    .timeControl(timeControl)
                    .currency(CurrencyCode.EUR)
                    .entryFee(BigDecimal.valueOf((i % 5) * 10L))
                    .edition(1)
                    .description("Demonstration tournament - not a real event")
                    .rated(i % 2 == 0)
                    .maximumParticipants(i % 5 == 0 ? 4 : 100)
                    .participationRequirements("Open to all players")
                    .paymentInstructions("Demo only - no real payment")
                    .status(status)
                    .organiser(organiser)
                    .createdOn(now)
                    .updatedOn(now)
                    .build();

            tournamentService.save(tournament);
        }
    }

    private void createDemoRegistrations(TournamentService tournamentService, UserService userService,
                                         TournamentRegistrationService tournamentRegistrationService) {

        LocalDateTime now = LocalDateTime.now();

        List<User> players = userService.findAll().stream()
                .filter(u -> u.getUsername().matches("demo_player_\\d{2}"))
                .sorted(Comparator.comparing(User::getUsername))
                .toList();

        if (players.size() < 20) {
            throw new IllegalStateException("Expected 20 demo players before creating registrations");
        }

        List<Tournament> tournaments = tournamentService.findAll().stream()
                .filter(t -> t.getName().startsWith("Demo Chess Tournament "))
                .sorted(Comparator.comparing(Tournament::getName))
                .toList();

        List<TournamentRegistration> registrations = new ArrayList<>();

        for (int tournamentIndex = 0; tournamentIndex < tournaments.size(); tournamentIndex++) {

            Tournament tournament = tournaments.get(tournamentIndex);

            if (tournamentIndex == 0) {
                continue;
            }

            boolean smallTournament = tournament.getMaximumParticipants() == 4;

            int participants = smallTournament ? (tournamentIndex % 2 == 0 ? 4 : 3) : 6 + (tournamentIndex % 3);

            for (int i = 0; i < participants; i++) {

                User player = players.get((tournamentIndex * 3 + i) % players.size());

                if (tournamentRegistrationService.findByTournamentIdAndPlayerId(
                        tournament.getId(), player.getId()).isPresent()) {
                    continue;
                }

                RegistrationStatus registrationStatus;
                PaymentStatus paymentStatus;

                LocalDateTime reservedUntil = null;
                LocalDateTime cancelledOn = null;
                LocalDateTime paymentSubmittedOn = null;
                String paymentReference = null;

                boolean isFree = tournament.isFree();

                int scenario = smallTournament ? 0 : i % 6;

                if (scenario == 0 || scenario == 1) {
                    registrationStatus = RegistrationStatus.CONFIRMED;
                    paymentStatus = isFree ? PaymentStatus.NOT_REQUIRED : PaymentStatus.CONFIRMED;

                } else if (scenario == 2 && !isFree) {
                    registrationStatus = RegistrationStatus.PENDING_PAYMENT;
                    paymentStatus = PaymentStatus.PENDING;
                    reservedUntil = now.plusMinutes(45);

                } else if (scenario == 3) {
                    registrationStatus = RegistrationStatus.CANCELLED;
                    paymentStatus = isFree ? PaymentStatus.NOT_REQUIRED : PaymentStatus.PENDING;
                    cancelledOn = now.minusDays(1);

                } else if (scenario == 4 && !isFree) {
                    registrationStatus = RegistrationStatus.EXPIRED;
                    paymentStatus = PaymentStatus.EXPIRED;
                    reservedUntil = now.minusHours(2);

                } else if (scenario == 5 && !isFree) {
                    registrationStatus = RegistrationStatus.PENDING_PAYMENT;
                    paymentStatus = PaymentStatus.SUBMITTED;
                    reservedUntil = now.plusHours(2);
                    paymentSubmittedOn = now.minusMinutes(10);
                    paymentReference = "DEMO-PAY-" + tournamentIndex + "-" + i;

                } else {
                    registrationStatus = RegistrationStatus.CONFIRMED;
                    paymentStatus = PaymentStatus.NOT_REQUIRED;
                }

                LocalDateTime registeredOn = now.minusDays(2);

                TournamentRegistration registration = TournamentRegistration.builder()
                        .tournament(tournament)
                        .player(player)
                        .registrationStatus(registrationStatus)
                        .paymentStatus(paymentStatus)
                        .paymentReference(paymentReference)
                        .registeredOn(registeredOn)
                        .updatedOn(now)
                        .cancelledOn(cancelledOn)
                        .reservedUntil(reservedUntil)
                        .paymentSubmittedOn(paymentSubmittedOn)
                        .hidden(false)
                        .build();

                registrations.add(registration);
            }
        }

        tournamentRegistrationService.seedDemoRegistrations(registrations);

        System.out.println("Created " + registrations.size() + " new demo registrations");
    }

    private void createDemoTravelGroups(TournamentService tournamentService, UserService userService,
                                        TravelGroupService travelGroupService) {

        LocalDateTime now = LocalDateTime.now();

        List<Tournament> tournaments = tournamentService.findAll()
                .stream()
                .filter(t -> t.getName().startsWith("Demo Chess Tournament "))
                .filter(t -> t.getStatus() == TournamentStatus.PUBLISHED)
                .filter(t -> t.getStartTime().isAfter(now.plusDays(2)))
                .sorted(Comparator.comparing(Tournament::getStartTime))
                .toList();

        if (tournaments.isEmpty()) {
            throw new IllegalStateException("No eligible demo tournaments for travel groups");
        }

        for (int i = 0; i < 15; i++) {

            String username = String.format("demo_player_%02d", i + 1);

            User owner = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Missing demo user: " + username));

            Tournament tournament = tournaments.get(i % tournaments.size());

            String groupName = String.format("Demo Travel Group %02d", i + 1);

            boolean exists = travelGroupService
                    .getTravelGroupsByUser(owner.getId())
                    .stream()
                    .anyMatch(g -> g.getName().equals(groupName));

            if (exists) {
                continue;
            }

            TravelGroupRequest request = new TravelGroupRequest();

            request.setName(groupName);
            request.setDescription("Demo travel group for " + tournament.getName());

            request.setTournamentId(tournament.getId());
            request.setDepartureCountry("Bulgaria");
            request.setDepartureCity("Sofia");
            request.setMeetingPoint("Sofia Central Station");

            request.setDepartureTime(tournament.getStartTime().minusDays(1));

            request.setTransportationType(TransportationType.values()[i % 3]);

            request.setMaximumMembers(i % 3 == 0 ? 3 : 5);
            request.setEstimatedCost(BigDecimal.valueOf(15 + i * 5L));
            request.setCurrency(CurrencyCode.EUR);

            travelGroupService.createTravelGroup(request, owner.getId());
        }
    }


    private void createDemoTravelRequests(UserService userService, TravelGroupService travelGroupService,
                                          TravelRequestService travelRequestService) {

        int created = 0;

        for (int groupNumber = 1; groupNumber <= 15; groupNumber++) {

            String ownerUsername = String.format("demo_player_%02d", groupNumber);

            User owner = userService.findByUsername(ownerUsername)
                    .orElseThrow(() -> new IllegalStateException("Missing demo owner: " + ownerUsername));

            String groupName = String.format("Demo Travel Group %02d", groupNumber);

            TravelGroup group = travelGroupService
                    .getTravelGroupsByUser(owner.getId())
                    .stream()
                    .filter(g -> groupName.equals(g.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Missing demo group: " + groupName));

            if (group.getStatus() != TravelGroupStatus.OPEN) {
                continue;
            }

            UUID groupId = group.getId();
            UUID ownerId = owner.getId();

            for (int j = 1; j <= 4; j++) {

                int playerNumber = ((groupNumber + j - 1) % 20) + 1;

                String playerUsername = String.format("demo_player_%02d", playerNumber);

                User player = userService
                        .findByUsername(playerUsername)
                        .orElseThrow();

                UUID playerId = player.getId();

                if (travelRequestService.hasRequestFromUser(groupId, playerId)) {
                    continue;
                }

                if (travelRequestService.countAvailableSpots(group) <= 0) {
                    break;
                }

                travelRequestService.createTravelRequest(group, playerId,
                        "Demo request from " + playerUsername);

                created++;

                TravelRequest request = travelRequestService
                        .getAllPendingRequests(groupId, TravelRequestStatus.PENDING)
                        .stream()
                        .filter(r -> r.getApplicant().getId().equals(playerId))
                        .findFirst()
                        .orElseThrow();

                int scenario = groupNumber % 3;

                if (scenario == 1) {
                    int availableSpots = travelRequestService.countAvailableSpots(group);

                    if (availableSpots > 0) {
                        travelRequestService.acceptTravelRequest(groupId, request.getId(), ownerId, availableSpots);
                    }

                } else if (scenario == 2) {
                    if (j == 1) {
                        int availableSpots = travelRequestService.countAvailableSpots(group);

                        travelRequestService.acceptTravelRequest(groupId, request.getId(), ownerId, availableSpots);

                    } else if (j == 2) {
                        travelRequestService.rejectTravelRequest(groupId, request.getId(), ownerId);
                    }
                }

            }
        }
        System.out.println("Created " + created + " new demo travel requests");
    }


    private void createDemoReviews(TournamentService tournamentService, TournamentRegistrationService tournamentRegistrationService,
                                   ReviewService reviewService) {

        Rating[] ratings = {
                Rating.EXCELLENT,
                Rating.GOOD,
                Rating.NEUTRAL,
                Rating.FAIR,
                Rating.POOR
        };

        String[] titles = {
                "Excellent tournament",
                "Very good organisation",
                "Enjoyable experience",
                "Could be improved",
                "Some organisational problems"
        };

        String[] contents = {
                "Excellent organisation, clear schedule and a great competitive atmosphere.",

                "Well organised tournament with helpful staff and interesting games.",

                "I enjoyed participating in this tournament and meeting other chess players.",

                "The tournament was enjoyable, but the schedule and communication could be improved.",

                "There were some delays and organisational issues. Hopefully the next edition will be better."
        };

        List<Tournament> tournaments = tournamentService.findAll()
                .stream()
                .filter(t -> t.getName().startsWith("Demo Chess Tournament "))
                .filter(t -> t.getStatus() == TournamentStatus.COMPLETED
                        || t.getStatus() == TournamentStatus.STARTED)
                .sorted(Comparator.comparing(Tournament::getName))
                .toList();

        int created = 0;

        for (Tournament tournament : tournaments) {
            UUID tournamentId = tournament.getId();

            List<TournamentRegistration> registrations = tournamentRegistrationService
                    .findAllByTournamentId(tournamentId);

            Set<UUID> existingAuthors = reviewService
                    .findAllByTournamentId(tournamentId)
                    .stream()
                    .map(review -> review.getAuthor().getId())
                    .collect(Collectors.toSet());

            int reviewIndex = existingAuthors.size();

            for (TournamentRegistration registration : registrations) {

                User player = registration.getPlayer();

                if (!player.getUsername().matches("demo_player_\\d{2}")) {
                    continue;
                }

                UUID playerId = player.getId();

                if (existingAuthors.contains(playerId)) {
                    continue;
                }

                if (playerId.equals(tournament.getOrganiser().getId())) {
                    continue;
                }

                int ratingIndex = reviewIndex % ratings.length;

                ReviewRequest request = new ReviewRequest();
                request.setRating(ratings[ratingIndex]);
                request.setTitle(titles[ratingIndex]);
                request.setContent(contents[ratingIndex]);

                reviewService.createReview(tournamentId, playerId, request);

                existingAuthors.add(playerId);
                created++;
                reviewIndex++;

                if (reviewIndex >= 5) {
                    break;
                }
            }
        }

        System.out.println("Created " + created + " new demo reviews");
    }

    private void createDemoGames(TournamentService tournamentService, TournamentRegistrationService registrationService,
                                 TournamentResultsService resultsService) {

        List<Tournament> tournaments = tournamentService.findAll()
                .stream()
                .filter(t -> t.getName().startsWith("Demo Chess Tournament "))
                .filter(t -> t.getStatus() == TournamentStatus.STARTED
                        || t.getStatus() == TournamentStatus.COMPLETED)
                .sorted(Comparator.comparing(Tournament::getName))
                .toList();

        int created = 0;

        GameResult[] results = {GameResult.WHITE_WIN, GameResult.DRAW, GameResult.BLACK_WIN};

        for (Tournament tournament : tournaments) {
            UUID tournamentId = tournament.getId();

            if (!resultsService.getGamesByTournament(tournamentId).isEmpty()) {
                continue;
            }

            List<TournamentRegistration> confirmed = registrationService.findAllByTournamentId(tournamentId)
                    .stream()
                    .filter(r -> r.getRegistrationStatus() == RegistrationStatus.CONFIRMED)
                    .sorted(Comparator.comparing(r -> r.getPlayer().getUsername()))
                    .toList();

            if (confirmed.size() < 2) {
                continue;
            }

            User player1 = confirmed.get(0).getPlayer();
            User player2 = confirmed.get(1).getPlayer();

            for (int round = 1; round <= 3; round++) {

                GameRequest request = new GameRequest();

                request.setRoundNumber(round);
                request.setBoardNumber(1);

                User white = round % 2 == 0 ? player2 : player1;
                User black = round % 2 == 0 ? player1 : player2;

                request.setWhitePlayerId(white.getId());
                request.setWhitePlayerUsername(white.getUsername());

                request.setBlackPlayerId(black.getId());
                request.setBlackPlayerUsername(black.getUsername());

                GameResult result = results[(round - 1) % results.length];

                request.setResult(result);
                request.setTotalMoves(25 + round * 8);

                request.setWinCondition(result == GameResult.DRAW ? WinCondition.AGREEMENT : WinCondition.RESIGNATION);

                request.setPlayedOn(tournament.getStartTime().plusHours(round));

                request.setOrganiserNotes("Demonstration game");

                resultsService.createGame(tournamentId, request);
                created++;
            }
        }

        System.out.println("Created " + created + " demo games");
    }


}

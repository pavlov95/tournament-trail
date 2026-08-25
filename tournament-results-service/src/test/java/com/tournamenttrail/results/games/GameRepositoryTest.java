package com.tournamenttrail.results.games;

import com.tournamenttrail.results.fixtures.GameFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    public void setUp() {
        gameRepository.deleteAll();

    }

    @Test
    void findAllByTournamentIdOrderByRoundNumberAscBoardNumberAsc_shouldReturnOnlyGamesForTournamentOrderedByRoundAndBoard() {
        UUID tournamentId = UUID.randomUUID();

        Game round2Board1 =GameFixture.create(tournamentId, 2, 1);
        Game round1Board2 = GameFixture.create(tournamentId, 1, 2);
        Game round1Board1 = GameFixture.create(tournamentId, 1, 1);
        Game otherTournamentGame = GameFixture.create(UUID.randomUUID(), 1, 1);

        gameRepository.saveAll(List.of(round2Board1, round1Board2, round1Board1, otherTournamentGame));

        gameRepository.flush();

        List<Game> result = gameRepository.findAllByTournamentIdOrderByRoundNumberAscBoardNumberAsc(tournamentId);

        assertThat(result).hasSize(3);

        assertThat(result)
                .extracting(Game::getTournamentId)
                .containsOnly(tournamentId);

        assertThat(result)
                .extracting(Game::getRoundNumber, Game::getBoardNumber)
                .containsExactly(
                        tuple(1, 1),
                        tuple(1, 2),
                        tuple(2, 1));
    }

    @Test
    void findByIdAndTournamentId_shouldReturnGame_whenGameBelongsToTournament() {
        Game game = GameFixture.create();
        gameRepository.save(game);

        Game result = gameRepository.findByIdAndTournamentId(game.getId(), game.getTournamentId()).orElseThrow();

        assertEquals(game.getId(), result.getId());
        assertEquals(game.getTournamentId(), result.getTournamentId());
    }

    @Test
    void findByIdAndTournamentId_shouldReturnEmpty_whenGameBelongsToDifferentTournament() {
        Game game = GameFixture.create();

        gameRepository.save(game);

        Optional<Game> result = gameRepository.findByIdAndTournamentId(game.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndTournamentId_shouldReturnEmpty_whenGameDoesNotExist() {
        Optional<Game> result = gameRepository.findByIdAndTournamentId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}
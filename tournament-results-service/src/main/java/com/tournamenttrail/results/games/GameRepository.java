package com.tournamenttrail.results.games;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {

    List<Game> findAllByTournamentIdOrderByRoundNumberAscBoardNumberAsc(UUID tournamentId);

    Optional<Game> findByIdAndTournamentId(UUID id, UUID tournamentId);
}
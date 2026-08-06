package com.tournamenttrail.results.games.dtos;

import com.tournamenttrail.results.games.enums.GameResult;
import com.tournamenttrail.results.games.enums.WinCondition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class GameRequest {

    @NotNull(message = "Round number is required.")
    @Min(value = 1, message = "Round number must be at least 1.")
    private Integer roundNumber;

    @NotNull(message = "Board number is required.")
    @Min(value = 1, message = "Board number must be at least 1.")
    private Integer boardNumber;

    @NotNull(message = "White player id is required.")
    private UUID whitePlayerId;

    @NotBlank(message = "White player username is required.")
    private String whitePlayerUsername;

    @NotNull(message = "Black player id is required.")
    private UUID blackPlayerId;

    @NotBlank(message = "Black player username is required.")
    private String blackPlayerUsername;

    @NotNull(message = "Game result is required.")
    private GameResult result;

    @Min(value = 0, message = "Total moves cannot be negative.")
    private Integer totalMoves;

    private WinCondition winCondition;

    @Size(max = 1000, message = "Organiser notes cannot exceed 1000 characters.")
    private String organiserNotes;

    private LocalDateTime playedOn;
}
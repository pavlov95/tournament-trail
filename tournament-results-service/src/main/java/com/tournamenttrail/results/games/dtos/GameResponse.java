package com.tournamenttrail.results.games.dtos;

import com.tournamenttrail.results.games.enums.GameResult;
import com.tournamenttrail.results.games.enums.WinCondition;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class GameResponse {

    private UUID id;

    private UUID tournamentId;

    private Integer roundNumber;

    private Integer boardNumber;

    private UUID whitePlayerId;

    private String whitePlayerUsername;

    private UUID blackPlayerId;

    private String blackPlayerUsername;

    private GameResult result;

    private BigDecimal whitePoints;

    private BigDecimal blackPoints;

    private Integer totalMoves;

    private WinCondition winCondition;

    private String organiserNotes;

    private LocalDateTime playedOn;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
package com.tournamenttrail.results.games;

import com.tournamenttrail.results.games.enums.GameResult;
import com.tournamenttrail.results.games.enums.WinCondition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tournamentId;

    @Column(nullable = false)
    private Integer roundNumber;

    @Column(nullable = false)
    private Integer boardNumber;

    @Column(nullable = false)
    private UUID whitePlayerId;

    @Column(nullable = false)
    private String whitePlayerUsername;

    @Column(nullable = false)
    private UUID blackPlayerId;

    @Column(nullable = false)
    private String blackPlayerUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameResult result;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal whitePoints;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal blackPoints;

    private Integer totalMoves;

    @Enumerated(EnumType.STRING)
    private WinCondition winCondition;

    @Column(length = 1000)
    private String organiserNotes;

    private LocalDateTime playedOn;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;
}
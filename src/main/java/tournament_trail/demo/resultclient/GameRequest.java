package tournament_trail.demo.resultclient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class GameRequest {

    private Integer roundNumber;
    private Integer boardNumber;
    private UUID whitePlayerId;
    private String whitePlayerUsername;
    private UUID blackPlayerId;
    private String blackPlayerUsername;
    private GameResult result;
    private Integer totalMoves;
    private WinCondition winCondition;
    private String organiserNotes;
    private LocalDateTime playedOn;
}
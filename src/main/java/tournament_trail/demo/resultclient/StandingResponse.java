package tournament_trail.demo.resultclient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class StandingResponse {

    private UUID playerId;
    private String username;
    private BigDecimal points;
    private int gamesPlayed;
    private int wins;
    private int draws;
    private int losses;
}
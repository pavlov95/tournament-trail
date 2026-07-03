package tournament_trail.demo.web.dtos;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import tournament_trail.demo.entities.enums.TimeControl;

import java.time.LocalDate;

@Data
public class TournamentSearchRequest {

    private String name;

    private String country;

    private String city;

    private TimeControl timeControl;

    private Boolean rated;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
}
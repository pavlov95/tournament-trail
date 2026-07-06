package tournament_trail.demo.web.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TransportationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TravelGroupRequest {
    @Size(max = 150, message = "Name must be less than 150 characters")
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull()
    private UUID tournamentId;

    @Size(max = 50, message = "Departing Country must be less than 50 characters")
    @NotBlank(message = "Departure Country is required")
    private String departureCountry;

    @Size(max = 50, message = "Departure city must be less than 50 characters")
    @NotBlank(message = "Departure city is required")
    private String departureCity;

    @Size(max = 100, message = "Meeting point must be less than 100 characters")
    @NotBlank(message = "Meeting point is required")
    private String meetingPoint;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime departureTime;

    @NotBlank(message = "Transportation Type is required")
    private TransportationType transportationType;

    @NotNull(message = "Members are required")
    @Positive(message = "Members can not be 0 or less")
    private Integer maximumMembers;

    @NotNull(message = "Estimated cost is required. Later you can change it")
    @DecimalMin(value = "0.00", message = "Entry fee cannot be negative")
    @Digits(integer = 8, fraction = 2,
            message = "Entry fee must have up to 8 integer digits and 2 decimal places")
    private BigDecimal estimatedCost;

    @NotNull(message = "Currency is required")
    private CurrencyCode currency;
}

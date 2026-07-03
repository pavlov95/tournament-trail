package tournament_trail.demo.web.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TimeControl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateTournamentRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Venue is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String venue;

    @NotNull(message = "Registration deadline is required")
    @Future(message = "Registration deadline must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime registrationDeadline;

    @NotNull(message = "Time control is required")
    private TimeControl timeControl;

    @NotBlank(message = "Country is required")
    @Size(min=3, max = 50, message = "Country name must be between 3 and 50 characters")
    private String country;

    @NotBlank(message = "City is required")
    @Size(min=3, max = 50, message = "City name must be between 3 and 50 characters" )
    private String city;

    @NotNull(message = "Entry fee is required")
    @DecimalMin(value = "0.00", message = "Entry fee cannot be negative")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Entry fee must have up to 8 integer digits and 2 decimal places"
    )
    private BigDecimal entryFee;

    @NotNull(message = "Currency is required")
    private CurrencyCode currency;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;


    @NotNull(message = "Edition is required")
    @Min(value = 1, message = "Edition can not be negative or zero")
    private Integer edition;

    @NotBlank
    private String description;

    private boolean rated;

    @NotNull(message = "Maximum participants is required")
    @Min(value = 2, message = "A tournament must allow at least 2 participants")
    private Integer maximumParticipants;

    @NotBlank
    @Size( max = 1000, message = "Participation requirements must be below 1000 characters")
    private String participationRequirements;

    @Size( max = 1000, message = "Payment instructions must be below 1000 characters")
    private String paymentInstructions;

}

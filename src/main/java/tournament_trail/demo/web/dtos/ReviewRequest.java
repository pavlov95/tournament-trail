package tournament_trail.demo.web.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tournament_trail.demo.entities.enums.Rating;

@Data
public class ReviewRequest {
    @NotNull(message = "You must choose a rating.")
    private Rating rating;

    @NotBlank(message = "Title must not be blank.")
    @Size(max = 100, message = "Title must be less than 100 characters.")
    private String title;

    @NotBlank(message = "Content of review must not be blank.")
    @Size(max = 500, message = "Content of review must be less than 500 characters.")
    private String content;

}

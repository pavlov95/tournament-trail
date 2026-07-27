package tournament_trail.demo.web.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "Comment content is required")
    @Size(max=1000, message = "Comment content should be less than 1000 characters")
    private String content;

}

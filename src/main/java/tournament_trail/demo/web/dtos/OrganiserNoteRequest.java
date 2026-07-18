package tournament_trail.demo.web.dtos;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganiserNoteRequest {
    @Size(max = 500)
    private String organiserNote;

}

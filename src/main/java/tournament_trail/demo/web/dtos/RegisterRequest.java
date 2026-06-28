package tournament_trail.demo.web.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @Size(min = 6, max = 30, message = "Username must be between 6 and 30 characters")

    private String username;

    @Size(min = 6, max = 30, message = "Password must be between 6 and 30 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
            message = "Password must contain an uppercase letter, a lowercase letter, and a number"
    )
    private String password;

    @Email
    private String email;
}

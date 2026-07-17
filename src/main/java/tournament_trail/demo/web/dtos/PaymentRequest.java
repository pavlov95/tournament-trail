package tournament_trail.demo.web.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentRequest {

    @Size(max = 100)
    @NotBlank
    private String paymentReference;


}

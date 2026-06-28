package tournament_trail.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import tournament_trail.demo.entities.enums.TravelRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "travel_requests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_travel_request_group_applicant",
                columnNames = {"travel_group_id", "applicant_id"}
        )
)
public class TravelRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "travel_group_id", nullable = false)
    private TravelGroup travelGroup;

    @ManyToOne
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TravelRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedOn;

    private LocalDateTime respondedOn;

}

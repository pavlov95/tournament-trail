package tournament_trail.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import tournament_trail.demo.entities.enums.PaymentStatus;
import tournament_trail.demo.entities.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tournament_registrations", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_tournament_player",
                columnNames = {"tournament_id", "player_id"}
        )
})
public class TournamentRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus registrationStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(length = 100)
    private String paymentReference;

    @Column(length = 500)
    private String organiserNote;

    @Column(nullable = false)
    private LocalDateTime registeredOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    private LocalDateTime cancelledOn;

}

package tournament_trail.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tournaments")
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TimeControl timeControl;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal entryFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currency;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private int edition;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean rated;

    @ManyToOne()
    @JoinColumn(name = "organiser_id", nullable = false)
    private User organiser;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Column(nullable = false)
    private int maximumParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status;

    @Column(nullable = false, length = 1000)
    private String participationRequirements;

    @Column(length = 1000)
    private String paymentInstructions;

    public boolean isFree() {
        return entryFee == null || entryFee.compareTo(BigDecimal.ZERO) == 0;
    }


}

package tournament_trail.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TransportationType;
import tournament_trail.demo.entities.enums.TravelGroupStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "travel_groups")
public class TravelGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 50)
    private String departureCountry;

    @Column(nullable = false, length = 50)
    private String departureCity;

    @Column(nullable = false, length = 100)
    private String meetingPoint;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportationType transportationType;

    @Column(nullable = false)
    private int maximumMembers;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyCode currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TravelGroupStatus status;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;
}

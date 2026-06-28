package tournament_trail.demo.entities;


import jakarta.persistence.*;
import lombok.*;
import tournament_trail.demo.entities.enums.Rating;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_tournament_author",
                columnNames = {"tournament_id", "author_id"}
        )
)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Rating rating;

    @Column(length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

}

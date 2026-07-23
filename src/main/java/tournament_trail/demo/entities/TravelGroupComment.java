package tournament_trail.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "travel_group_comments")
public class TravelGroupComment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "travel_group_id", nullable = false)
    private TravelGroup travelGroup;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private boolean pinned;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime editedOn;

    @Column(nullable = false)
    private boolean hidden;
}

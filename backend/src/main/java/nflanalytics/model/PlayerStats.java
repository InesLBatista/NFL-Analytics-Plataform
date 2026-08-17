package nflanalytics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    //passing QB
    private Integer passingAttempts;
    private Integer passingCompletions;
    private Integer passingYards;
    private Integer passingTouchdowns;
    private Integer interceptions;
    //rushing RB, QB
    private Integer rushingAttempts;
    private Integer rushingYards;
    private Integer rushingTouchdowns;
    //receiving WT, TE, RB
    private Integer targets;
    private Integer receptions;
    private Integer receivingYards;
    private Integer receivingTouchdowns;
    //defensive
    private Integer tackles;
    private Integer sacks;
    private Integer forcedFumbles;

    //rating pass by nfl's official formula
    @Transient  
    public Double getPasserRating() {
        if (passingAttempts == null || passingAttempts == 0) return null;
        double comp = ((double) passingCompletions / passingAttempts - 0.3) * 5;
        double yds = ((double) passingYards / passingAttempts - 3) * 0.25;
        double td = ((double) passingTouchdowns / passingAttempts) * 20;
        double intPct = 2.375 - ((double) interceptions / passingAttempts) * 25;
        return (comp + yds + td + intPct) / 6 * 100;
    }
}

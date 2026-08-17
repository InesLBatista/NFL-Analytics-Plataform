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
@Table(name = "game_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    //offensive 
    private Integer totalYards;
    private Integer passingYards;
    private Integer rushingYards;
    private Integer turnovers;
    private Integer thirdDownConversions;
    private Integer thirdDownAttempts;
    private Double timeOfPossessionMinutes;
    //defensive 
    private Integer sacks;
    private Integer penalties;
    private Integer penaltyYards;

    //calculated in service, not saved in input
    @Transient //calculated in runtime with the parameters above
    public Double getThirdDownPct() {
        if (thirdDownAttempts == null || thirdDownAttempts == 0) return 0.0;
        return (double) thirdDownConversions / thirdDownAttempts * 100;
    }
}

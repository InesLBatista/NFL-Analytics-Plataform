package nflanalytics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "snap_counts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    private String playerName;
    private String team;
    private String position;
    private Integer season;
    private Integer week;
    private Integer offenseSnaps;
    private Double offensePct;
    private Integer defenseSnaps;
    private Double defensePct;
    private Integer specialTeamsSnaps;
    private Double specialTeamsPct;
}

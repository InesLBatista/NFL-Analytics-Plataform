package nflanalytics.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "depth_chart_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepthChartEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    private Integer season;
    private Integer week;
    private String team;

    private String position;        
    private String depthPosition;  

    //1=starter, 2=first substitute, 3=second substitute, etc
    private Integer depthRank;

    private String playerName; 
}
package nflanalytics.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "nextgen_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextGenStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    private String statType;
    private Integer season;
    private Integer week;
    private String team;

    //passing
    private Double avgTimeToThrow;
    private Double avgCompletedAirYards;
    private Double avgIntendedAirYards;
    private Double avgAirYardsDifferential;
    private Double aggressiveness;
    private Double maxCompletedAirDistance;
    private Double completionPctAboveExpectation;
    //rushing
    private Double rushYardsOverExpected;
    private Double rushYardsOverExpectedPerAtt;
    private Double rushPctOverExpected;
    private Double efficiency;
    private Double avgTimeToLos;
    //receving
    private Double avgCushion;
    private Double avgSeparation;
    private Double avgYac;
    private Double avgExpectedYac;
    private Double avgYacAboveExpectation;
    private Double catchPct;
}
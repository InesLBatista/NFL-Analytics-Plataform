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
@Table(name = "combine_results")
@Data
@NoArgsConstructor  
@AllArgsConstructor
public class CombineResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    private String playerName;
    private Integer season;
    private String position;
    private String college;

    private Double heightIn;
    private Double weightLb;
    private Double fortyYardDash;
    private Double benchPressReps;
    private Double verticalJumpIn;
    private Double broadJumpIn;
    private Double threeConeDrill;
    private Double twentyYardShuttle;
}

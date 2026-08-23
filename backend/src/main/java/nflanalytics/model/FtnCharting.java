package nflanalytics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//tactical context for pbp
@Entity
@Table(name = "ftn_chartting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FtnCharting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "play_by_play_id", unique = true)
    private PlayByPlay play;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    private Integer season;
    private Integer week;

    private String startingHash;  
    private String qbLocation;   
    private Integer offenseBackfieldCount;
    private Integer defenseBoxCount;

    private Boolean isNoHuddle;
    private Boolean isMotion;
    private Boolean isPlayAction;
    private Boolean isScreenPass;
    private Boolean isRpo;
    private Boolean isTrickPlay;
    private Boolean isQbOutOfPocket;
    private Boolean isInterceptionWorthy;
    private Boolean isThrowAway;
    private Boolean isCatchableBall;
    private Boolean isContestedBall;
    private Boolean isDrop;
    private Boolean isQbSneak;
}

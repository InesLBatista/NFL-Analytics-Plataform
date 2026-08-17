package nflanalytics.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(nullable = false)
    private LocalDateTime gameTeam;

    @Column(nullable = false)
    private Integer week;

    @Column(nullable = false)
    private Integer season;

    private Integer homeScore;
    private Integer awayScore;

    private String stadium;
    private String stadiumId;
    private String roof;
    private String surface;
    private Integer temp;
    private Integer wind;

    private String homeCoach;
    private String awayCoach;
    private String homeQbName;
    private String awayQbName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    public enum GameStatus {
        SCHEDULED, IN_PROGRESS, FINAL
    }
}

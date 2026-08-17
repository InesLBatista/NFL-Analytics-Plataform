package nflanalytics.model;

import jakarta.persistence.Column;
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
@Table(name = "play_by_play")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayByPlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    private Integer season;
    private Integer week;

    @Column(unique = true)
    private String externalPlayId; //"{game_id_nflverse}_{play_id}"

    private Integer quarter;
    private Integer down;
    private Integer yardsToGo; 
    private Integer yardlineNumber;

     private String playType; 

    @Column(columnDefinition = "TEXT") 
    private String description;

    private String posTeam;
    private String defTeam;

    private Integer yardsGained;

    private Double epa; //expected points added
    private Double wpa; //win probability added
    private Boolean success;

    private Boolean touchdown;
    private Boolean interception;
    private Boolean fumble;
    private Boolean sack;
    private Boolean penalty;

    private String passerName;
    private String rusherName;
    private String receiverName;

    private Integer gameSecondsRemaining;
    private Integer posteamScore;
    private Integer defteamScore;
}
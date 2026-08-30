package nflanalytics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamePrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "game_id", unique = true, nullable = false)
    private Game game;

    private Double homeWinProbability; 
    private Double homeEloAtPrediction;
    private Double awayEloAtPrediction;

    private Boolean predictionCorrect;
}

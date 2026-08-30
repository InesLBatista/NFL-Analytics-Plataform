package nflanalytics.repository;

import nflanalytics.model.GamePrediction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamePredictionRepository extends JpaRepository<GamePrediction, Long> {
    GamePrediction findByGame_Id(Long gameId);
}

package nflanalytics.repository;

import nflanalytics.model.CombineResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CombineResultRepository extends JpaRepository<CombineResult, Long> {
    boolean existsByPlayerNameAndSeason(String playerName, Integer season);
}
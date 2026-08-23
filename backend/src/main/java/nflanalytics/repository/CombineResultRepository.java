package nflanalytics.repository;

import nflanalytics.model.CombineResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CombineResultRepository extends JpaRepository<CombineResult, Long> {
    boolean existsByPlayerNameAndSeason(String playerName, Integer season);
    //combine results for a specific draft class year
    List<CombineResult> findBySeason(Integer season);
    //all combine entries linked to a player (should be one, but player can have multiple entries if data has duplicates)
    List<CombineResult> findByPlayer_Id(Long playerId);
}
package nflanalytics.repository;

import nflanalytics.model.FtnCharting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FtnChartingRepository extends JpaRepository<FtnCharting, Long> {
    List<FtnCharting> findByGame_Id(Long gameId);
    boolean existsByPlay_Id(Long playId);
}
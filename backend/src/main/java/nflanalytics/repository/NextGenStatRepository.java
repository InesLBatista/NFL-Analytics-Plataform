package nflanalytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.NextGenStat;

import java.util.List;

public interface NextGenStatRepository extends JpaRepository<NextGenStat, Long> {
    boolean existsByPlayer_IdAndSeasonAndWeekAndStatType(Long playerId, Integer season, Integer week, String statType);

    List<NextGenStat> findByPlayer_IdAndSeasonOrderByWeek(Long playerId, Integer season);

    List<NextGenStat> findByGame_Id(Long gameId);
}

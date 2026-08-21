package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.DepthChartEntry;

public interface DepthChartRepository extends JpaRepository<DepthChartEntry, Long> {
    boolean existsByPlayerNameAndTeamAndSeasonAndWeekAndPosition(String playerName, String team, Integer season, Integer week, String position);

    List<DepthChartEntry> findByTeamAndSeasonAndWeek(String team, Integer season, Integer week);

    DepthChartEntry findByTeamAndSeasonAndWeekAndPositionAndDepthRank(String team, Integer season, Integer week, String position, Integer depthRank);
}

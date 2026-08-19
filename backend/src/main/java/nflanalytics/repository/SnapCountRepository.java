package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.SnapCount;

public interface SnapCountRepository extends JpaRepository<SnapCount, Long> {
    boolean existsByPlayerNameAndTeamAndSeasonAndWeek(String playerName, String team, Integer season, Integer week);

    List<SnapCount> findByPlayer_IdAndSeasonOrderByWeek(Long playerId, Integer season);

    List<SnapCount> findByGame_Id(Long gameId);
}

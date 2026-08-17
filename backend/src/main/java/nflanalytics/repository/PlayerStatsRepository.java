package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.PlayerStats;

public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {
    List<PlayerStats> findByPlayer_Id(Long playerId);

    List<PlayerStats> findByGame_Id(Long gameId);

    //prevent duplicates
    boolean existsByPlayer_IdAndGame_Id(Long playerId, Long gameId);
}

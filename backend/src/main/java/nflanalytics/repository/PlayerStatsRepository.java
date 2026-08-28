package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nflanalytics.model.Player;
import nflanalytics.model.PlayerStats;

public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {
    List<PlayerStats> findByPlayer_Id(Long playerId);

    List<PlayerStats> findByGame_Id(Long gameId);

    //prevent duplicates
    boolean existsByPlayer_IdAndGame_Id(Long playerId, Long gameId);

    
    List<PlayerStats> findByPlayer_IdAndGame_Season(Long playerId, Integer season);

    @Query("SELECT DISTINCT ps.player FROM PlayerStats ps WHERE ps.game.season = :season")
    List<Player> findDistinctPlayersBySeason(@Param("season") Integer season);
}

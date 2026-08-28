package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.GameStats;

public interface GameStatsRepository extends JpaRepository<GameStats, Long> {
    List<GameStats> findByGame_Id(Long gameId);

    //use on the import     
    boolean existsByGame_IdAndTeam_Id(Long gameId, Long teamId);

    List<GameStats> findByTeam_IdAndGame_Season(Long teamId, Integer season);
}

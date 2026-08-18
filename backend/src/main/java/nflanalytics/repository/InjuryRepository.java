package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.Injury;

public interface InjuryRepository extends JpaRepository<Injury, Long> {
    boolean existsByPlayer_IdAndSeasonAndWeek(Long playerId, Integer season, Integer week);
    //base to calculate time not played for a certain player
    List<Injury> findByPlayer_IdAndSeasonOrderByWeek(Long playerId, Integer season);
    //injuries from both teams on a game to integrate the generated report
    List<Injury> findByGame_Id(Long gameId);
}

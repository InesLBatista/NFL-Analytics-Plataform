package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.DraftPick;

public interface DraftPickRepository extends JpaRepository<DraftPick, Long> {
    boolean existsBySeasonAndRoundAndPick(Integer season, Integer round, Integer pick);
    List<DraftPick> findBySeason(Integer season);
}

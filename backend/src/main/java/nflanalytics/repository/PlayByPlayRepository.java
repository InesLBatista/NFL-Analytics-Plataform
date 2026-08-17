package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.PlayByPlay;

public interface PlayByPlayRepository extends JpaRepository<PlayByPlay, Long> {
    Long countBySeason(Integer season);

    List<PlayByPlay> findByGame_IdOrderById(Long gameId);
}

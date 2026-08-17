package nflanalytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.GameReport;

public interface GameReportRepository extends JpaRepository<GameReport, Long> {
    GameReport findByGame_Id(Long gameId);
}


package nflanalytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Team findByAbbreviation(String abbreviation);
}

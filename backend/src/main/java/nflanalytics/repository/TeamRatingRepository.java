package nflanalytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.TeamRating;

public interface TeamRatingRepository extends JpaRepository<TeamRating, Long> {
    TeamRating findTopByTeam_IdAndSeasonLessThanEqualOrderBySeasonDescWeekDesc(Long teamId, Integer season);
}

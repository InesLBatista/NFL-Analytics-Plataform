package nflanalytics.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Game;
import nflanalytics.model.Team;
import nflanalytics.model.TeamRating;
import nflanalytics.repository.GameRepository;
import nflanalytics.repository.TeamRatingRepository;

@Service
@RequiredArgsConstructor
public class EloRatingService {
    private final GameRepository gameRepository;
    private final TeamRatingRepository teamRatingRepository;

    private static final double BASE_RATING = 1500.0;
    private static final double HOME_ADVANTAGE = 65.0;
    private static final double K_FACTOR = 20.0;
    private static final double REGRESSION_TO_MEAN = 1.0/3.0;

    //recalculating all ratings from 0 going throught all game in cronological order
    //calling it deletes history on previous ratings 
    //needed for when importing new games or parametrs change on the model
    public void recomputeAllRatings() {
        teamRatingRepository.deleteAll();

        List<Game> allGames = gameRepository.findAll();
        allGames.sort(Comparator
                .comparing(Game::getSeason)
                .thenComparing(Game::getWeek)
        );

        Map<Long, Double> currentRatings = new HashMap<>();
        int lastSeasonProcessed = -1;

        for (Game game : allGames) {
            if(game.getStatus() != Game.GameStatus.FINAL) continue;
            if(game.getHomeScore() == null || game.getAwayScore() == null) continue;

            //in the start of a new season, will regress according to the average
            if (game.getSeason() != lastSeasonProcessed) {
                regressRatingsToMean(currentRatings);
                lastSeasonProcessed = game.getSeason();
            }

            Team home = game.getHomeTeam();
            Team away = game.getAwayTeam();

            double homeRating = currentRatings.getOrDefault(home.getId(), BASE_RATING);
            double awayRating = currentRatings.getOrDefault(away.getId(), BASE_RATING);

            //awaited probability of home's victory
            double expectedHomeWin = 1.0/(1.0+Math.pow(10, (awayRating - homeRating - HOME_ADVANTAGE)/400.0));
            //1.0=home's victory, 0.5=draw, 0.0=home's loss
            double actualHomeResult;
            if (game.getHomeScore() > game.getAwayScore()) actualHomeResult = 1.0;
            else if (game.getHomeScore() < game.getAwayScore()) actualHomeResult = 0.0;
            else actualHomeResult = 0.5;

            
            //the margin of victory amplifies the adjustment
            double marginMultiplier = calculateMarginMultiplier(game.getHomeScore(), game.getAwayScore(), homeRating, awayRating);
            double newHomeRating = homeRating + K_FACTOR * marginMultiplier * (actualHomeResult - expectedHomeWin);
            double newAwayRating = awayRating + K_FACTOR * marginMultiplier * (actualHomeResult - expectedHomeWin);

            currentRatings.put(home.getId(), newHomeRating);
            currentRatings.put(away.getId(), newAwayRating);

            saveRatingSnapshot(home, game, newHomeRating);
            saveRatingSnapshot(away, game, newAwayRating);
        }
    }

    private void regressRatingsToMean(Map<Long, Double> ratings) {
        for (Long teamId : new HashSet<>(ratings.keySet())) {
            double current = ratings.get(teamId);
            double regressed = current + (BASE_RATING - current) * REGRESSION_TO_MEAN;
            ratings.put(teamId, regressed);
        }
    }

    private double calculateMarginMultiplier(int homeScore, int awayScore, double homeRating, double awayRating) {
        int scoreDiff = Math.abs(homeScore - awayScore);
        double eloDiff = homeRating - awayRating;
        return Math.log(scoreDiff + 1) * (2.2 / ((eloDiff * 0.001) + 2.2));
    }

    private void saveRatingSnapshot(Team team, Game game, double rating) {
        TeamRating snapshot = new TeamRating();
        snapshot.setTeam(team);
        snapshot.setGame(game);
        snapshot.setSeason(game.getSeason());
        snapshot.setWeek(game.getWeek());
        snapshot.setEloRating(rating);
        teamRatingRepository.save(snapshot);
    }

    //returns the most recent rating of a team know before a specific season
    public double getLatestRatingBeforeSeason(Long teamId, Integer season) {
        TeamRating rating = teamRatingRepository
                .findTopByTeam_IdAndSeasonLessThanEqualOrderBySeasonDescWeekDesc(teamId, season - 1);
        return (rating != null) ? rating.getEloRating() : BASE_RATING;
    }
}

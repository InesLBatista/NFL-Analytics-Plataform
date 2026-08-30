package nflanalytics.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Game;
import nflanalytics.model.GamePrediction;
import nflanalytics.repository.GamePredictionRepository;
import nflanalytics.repository.GameRepository;
import nflanalytics.repository.TeamRatingRepository;

@Service
@RequiredArgsConstructor
public class PredictionService {
    private final GameRepository gameRepository;
    private final TeamRatingRepository teamRatingRepository;
    private final GamePredictionRepository gamePredictionRepository;

    private static final double HOME_ADVANTAGE = 65.0;

    public GamePrediction predictGame(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        //looking for the most recent ranking for each team, during the same season on the passing week
        double homeRating = getRatingBeforeWeek(game.getHomeTeam().getId(), game.getSeason(), game.getWeek());
        double awayRating = getRatingBeforeWeek(game.getAwayTeam().getId(), game.getSeason(), game.getWeek());

        double homeWinProb = 1.0/(1.0+Math.pow(10, (awayRating - homeRating - HOME_ADVANTAGE)/400.0));

        GamePrediction prediction = gamePredictionRepository.findByGame_Id(gameId);
        if (prediction == null) {
            prediction = new GamePrediction();
            prediction.setGame(game);
        }

        prediction.setHomeWinProbability(homeWinProb);
        prediction.setHomeEloAtPrediction(homeRating);
        prediction.setAwayEloAtPrediction(awayRating);

        return gamePredictionRepository.save(prediction);
    }

    private double getRatingBeforeWeek(Long teamId, Integer season, Integer week) {
        var rating = teamRatingRepository.findTopByTeam_IdAndSeasonLessThanEqualOrderBySeasonDescWeekDesc(teamId, season);

        if (rating != null && (rating.getSeason() < season || rating.getWeek() < week)) {
            return rating.getEloRating();
        }
        return 1500.0;
    }


    public void evaluatePrediction(Long gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        GamePrediction prediction = gamePredictionRepository.findByGame_Id(gameId);

        if (game == null || prediction == null || game.getHomeScore() == null || game.getAwayScore() == null) {
            return;
        }

        boolean homeWon = game.getHomeScore() > game.getAwayScore();
        boolean predictedHomeWin = prediction.getHomeWinProbability() >= 0.5;

        prediction.setPredictionCorrect(homeWon == predictedHomeWin);
        gamePredictionRepository.save(prediction);
    }
}

package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.dto.PredictionAccuracyResult;
import nflanalytics.model.Game;
import nflanalytics.model.GamePrediction;
import nflanalytics.repository.GamePredictionRepository;
import nflanalytics.repository.GameRepository;
import nflanalytics.service.EloRatingService;
import nflanalytics.service.PredictionService;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final EloRatingService eloRatingService;
    private final GamePredictionRepository gamePredictionRepository;
    private final GameRepository gameRepository;

    //retrieve the stored prediction for a game if it exists
    @GetMapping("/game/{gameId}")
    public ResponseEntity<GamePrediction> getPrediction(@PathVariable Long gameId) {
        GamePrediction prediction = gamePredictionRepository.findByGame_Id(gameId);
        if (prediction == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(prediction);
    }

    //generate or update the prediction for a single game using the Elo ratings at that point in time
    @PostMapping("/game/{gameId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GamePrediction> predictGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(predictionService.predictGame(gameId));
    }

    //evaluate the prediction for a completed game — sets predictionCorrect based on the final score
    @PostMapping("/game/{gameId}/evaluate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> evaluatePrediction(@PathVariable Long gameId) {
        predictionService.evaluatePrediction(gameId);
        return ResponseEntity.ok().build();
    }

    //generate predictions for all games in a given week
    //useful for running the full slate before kickoff
    @PostMapping("/season/{season}/week/{week}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> predictWeek(
            @PathVariable int season,
            @PathVariable int week) {
        List<Game> games = gameRepository.findBySeasonAndWeek(season, week);
        int count = 0;
        for (Game game : games) {
            predictionService.predictGame(game.getId());
            count++;
        }
        return ResponseEntity.ok("Generated predictions for " + count + " games (season " + season + ", week " + week + ")");
    }

    //evaluate all predictions for a given week against final scores
    @PostMapping("/season/{season}/week/{week}/evaluate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> evaluateWeek(
            @PathVariable int season,
            @PathVariable int week) {
        List<Game> games = gameRepository.findBySeasonAndWeek(season, week);
        int count = 0;
        for (Game game : games) {
            predictionService.evaluatePrediction(game.getId());
            count++;
        }
        return ResponseEntity.ok("Evaluated predictions for " + count + " games (season " + season + ", week " + week + ")");
    }

    //accuracy summary for a specific week — how many predictions were correct out of those evaluated
    @GetMapping("/accuracy/season/{season}/week/{week}")
    public ResponseEntity<PredictionAccuracyResult> getWeekAccuracy(
            @PathVariable int season,
            @PathVariable int week) {
        List<Game> games = gameRepository.findBySeasonAndWeek(season, week);
        return ResponseEntity.ok(computeAccuracy(games, season, week));
    }

    //accuracy summary across the full season
    @GetMapping("/accuracy/season/{season}")
    public ResponseEntity<PredictionAccuracyResult> getSeasonAccuracy(@PathVariable int season) {
        List<Game> games = gameRepository.findBySeason(season);
        return ResponseEntity.ok(computeAccuracy(games, season, null));
    }

    //rebuilds the entire Elo rating history from scratch, processing all games in chronological order
    //necessary after importing new games or changing model parameters
    //this operation deletes all existing ratings before recomputing
    @PostMapping("/elo/recompute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> recomputeElo() {
        eloRatingService.recomputeAllRatings();
        return ResponseEntity.ok("Elo ratings recomputed successfully");
    }

    //calculates accuracy from a list of games by joining with stored predictions
    private PredictionAccuracyResult computeAccuracy(List<Game> games, int season, Integer week) {
        int evaluated = 0;
        int correct   = 0;

        for (Game game : games) {
            GamePrediction prediction = gamePredictionRepository.findByGame_Id(game.getId());
            if (prediction == null || prediction.getPredictionCorrect() == null) continue;
            evaluated++;
            if (Boolean.TRUE.equals(prediction.getPredictionCorrect())) correct++;
        }

        int incorrect   = evaluated - correct;
        double accuracy = evaluated > 0 ? (double) correct / evaluated * 100.0 : 0.0;

        return new PredictionAccuracyResult(season, week, evaluated, correct, incorrect, accuracy);
    }
}

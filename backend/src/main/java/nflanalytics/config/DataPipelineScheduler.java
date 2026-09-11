package nflanalytics.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Game;
import nflanalytics.repository.GameRepository;
import nflanalytics.service.DocumentChunkService;
import nflanalytics.service.EloRatingService;
import nflanalytics.service.GameReportService;
import nflanalytics.service.NflverseImportService;
import nflanalytics.service.PredictionService;
import nflanalytics.service.SeasonWeekCalculator;

@Component
@RequiredArgsConstructor
public class DataPipelineScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataPipelineScheduler.class);

    private final NflverseImportService importService;
    private final SeasonWeekCalculator seasonWeekCalculator;
    private final GameRepository gameRepository;
    private final EloRatingService eloRatingService;
    private final PredictionService predictionService;
    private final GameReportService gameReportService;
    private final DocumentChunkService documentChunkService;

    @Value("${automation.weekly-data-import-enabled:false}")
    private boolean weeklyImportEnabled;

    @Value("${automation.current-season}")
    private int currentSeason;

    //runs after Monday Night Football has finished
    //each step is guarded individually so a failure in one does not abort the rest
    @Scheduled(cron = "0 0 6 * * TUE")
    public void runWeeklyDataImport() {
        if (!weeklyImportEnabled) return;

        int week = seasonWeekCalculator.getCurrentWeek(currentSeason);
        log.info("Automated weekly pipeline starting — season {}, week {}", currentSeason, week);

        //import raw data from nflverse
        runStep("games import",        () -> importService.importGames(currentSeason));
        runStep("game stats import",   () -> importService.importGameStats(currentSeason));
        runStep("player stats import", () -> importService.importPlayerStats(currentSeason));
        runStep("play-by-play import", () -> importService.importPlayByPlay(currentSeason));
        runStep("injuries import",     () -> importService.importInjuries(currentSeason));
        runStep("snap counts import",  () -> importService.importSnapCounts(currentSeason));
        runStep("depth charts import", () -> importService.importDepthCharts(currentSeason));

        //update Elo ratings incrementally for games completed this week
        runStep("Elo incremental update", () -> {
            List<Game> weekGames = gameRepository.findBySeasonAndWeek(currentSeason, week - 1);
            for (Game game : weekGames) {
                eloRatingService.updateRatingsForGame(game);
            }
        });

        //generate win probability predictions for the upcoming week
        runStep("predictions for week " + week, () -> {
            List<Game> upcomingGames = gameRepository.findBySeasonAndWeek(currentSeason, week);
            for (Game game : upcomingGames) {
                predictionService.predictGame(game.getId());
            }
        });

        //evaluate predictions made last week against final scores
        runStep("evaluate predictions for week " + (week - 1), () -> {
            List<Game> lastWeekGames = gameRepository.findBySeasonAndWeek(currentSeason, week - 1);
            for (Game game : lastWeekGames) {
                predictionService.evaluatePrediction(game.getId());
            }
        });

        //generate AI game reports for completed games that don't have one yet
        runStep("game reports for week " + (week - 1), () -> gameReportService.generateWeekReports(currentSeason, week - 1));

        // step 6: index new game reports and player summaries into the RAG vector store
        runStep("RAG game report indexing",        () -> documentChunkService.indexPending());
        runStep("RAG player summary indexing",     () -> documentChunkService.indexPlayerSeasonSummaries(currentSeason));
        runStep("RAG team summary indexing",       () -> documentChunkService.indexTeamSeasonSummaries(currentSeason));

        log.info("Automated weekly pipeline completed — season {}, week {}", currentSeason, week);
    }

    //wraps each step so a failure is logged but does not abort the pipeline
    private void runStep(String name, ThrowingRunnable step) {
        try {
            log.info("Pipeline step starting: {}", name);
            step.run();
            log.info("Pipeline step completed: {}", name);
        } catch (Exception e) {
            log.error("Pipeline step failed: {} — {}", name, e.getMessage());
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}

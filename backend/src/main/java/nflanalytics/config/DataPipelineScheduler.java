package nflanalytics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import nflanalytics.service.NflverseImportService;
import nflanalytics.service.SeasonWeekCalculator;

@Component 
@RequiredArgsConstructor 
public class DataPipelineScheduler {
    private final NflverseImportService importService;
    private final SeasonWeekCalculator seasonWeekCalculator;

    @Value("${automation.weekly-data-import-enabled:false}")
    private boolean weeklyImportEnabled;

    @Value("${automation.current-season}")
    private int currentSeason;

    @Scheduled(cron = "0 0 6 * * TUE")
    public void runWeeklyDataImport() {
        if (!weeklyImportEnabled) return;

        try {
            int week = seasonWeekCalculator.getCurrentWeek(currentSeason);
            System.out.println("Automated Weekly Import: season " + currentSeason + ", week " + week);

            importService.importGames(currentSeason);
            importService.importGameStats(currentSeason);
            importService.importPlayerStats(currentSeason);
            importService.importPlayByPlay(currentSeason);
            importService.importInjuries(currentSeason);
            importService.importSnapCounts(currentSeason);

            System.out.println("Automated Weekly Import completed semanal");
        } catch (Exception e) {
            // nunca deixa uma falha na importação agendada derrubar a aplicação
            System.out.println("Error on the weekly import: " + e.getMessage());
        }
    }
}

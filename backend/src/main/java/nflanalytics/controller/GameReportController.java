package nflanalytics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.dto.WeekReportResult;
import nflanalytics.model.GameReport;
import nflanalytics.service.GameReportService;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class GameReportController {

    private final GameReportService gameReportService;

    @GetMapping("/api/games/{gameId}/report")
    public GameReport getReport(@PathVariable Long gameId) {
        return gameReportService.getReport(gameId);
    }

    @PostMapping("/api/admin/games/{gameId}/generate-report")
    public GameReport generateReport(@PathVariable Long gameId) throws Exception {
        return gameReportService.generateReport(gameId);
    }

    //generates reports for every game in a week in a single call
    //returns a summary with per-game error details so the caller knows exactly what happened
    @PostMapping("/api/admin/reports/season/{season}/week/{week}")
    public ResponseEntity<WeekReportResult> generateWeekReports(
            @PathVariable int season,
            @PathVariable int week) {
        WeekReportResult result = gameReportService.generateWeekReports(season, week);
        return ResponseEntity.ok(result);
    }
}

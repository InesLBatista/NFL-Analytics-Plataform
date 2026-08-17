package nflanalytics.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
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
}
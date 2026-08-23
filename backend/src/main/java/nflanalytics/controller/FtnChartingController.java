package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.FtnCharting;
import nflanalytics.service.FtnChartingService;

@RestController
@RequestMapping("/api/ftn-charting")
@RequiredArgsConstructor
public class FtnChartingController {

    private final FtnChartingService ftnChartingService;

    @GetMapping("/{id}")
    public ResponseEntity<FtnCharting> getChartingById(@PathVariable Long id) {
        return ResponseEntity.ok(ftnChartingService.getChartingById(id));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<FtnCharting>> getChartingByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(ftnChartingService.getChartingByGame(gameId));
    }
}

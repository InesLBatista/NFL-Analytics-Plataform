package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.service.PlayerUsageService;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerUsageController {

    private final PlayerUsageService playerUsageService;

    @GetMapping("/{playerId}/average-snap-pct")
    public ResponseEntity<Double> getAverageOffenseSnapPct(
            @PathVariable Long playerId,
            @RequestParam Integer season) {
        return ResponseEntity.ok(playerUsageService.getAverageOffenseSnapPct(playerId, season));
    }

    @GetMapping("/{playerId}/usage-drops")
    public ResponseEntity<List<String>> getUsageDrops(
            @PathVariable Long playerId,
            @RequestParam Integer season) {
        return ResponseEntity.ok(playerUsageService.detectUsageDrops(playerId, season));
    }
}

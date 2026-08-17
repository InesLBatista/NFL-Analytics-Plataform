package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.GameStats;
import nflanalytics.service.GameStatsService;

@RestController
@RequestMapping("/api/game-stats")
@RequiredArgsConstructor
public class GameStatsController {

    private final GameStatsService gameStatsService;

    @GetMapping("/{id}")
    public ResponseEntity<GameStats> getStatsById(@PathVariable Long id) {
        return ResponseEntity.ok(gameStatsService.getStatsById(id));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<GameStats>> getStatsByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameStatsService.getStatsByGame(gameId));
    }
}

package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.PlayerStats;
import nflanalytics.service.PlayerStatsService;

@RestController
@RequestMapping("/api/player-stats")
@RequiredArgsConstructor
public class PlayerStatsController {

    private final PlayerStatsService playerStatsService;

    @GetMapping("/{id}")
    public ResponseEntity<PlayerStats> getStatsById(@PathVariable Long id) {
        return ResponseEntity.ok(playerStatsService.getStatsById(id));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<PlayerStats>> getStatsByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(playerStatsService.getStatsByPlayer(playerId));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<PlayerStats>> getStatsByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(playerStatsService.getStatsByGame(gameId));
    }
}

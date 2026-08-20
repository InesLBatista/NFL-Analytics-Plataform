package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.NextGenStat;
import nflanalytics.service.NextGenStatService;

@RestController
@RequestMapping("/api/next-gen-stats")
@RequiredArgsConstructor
public class NextGenStatController {
    private final NextGenStatService nextGenStatService;

    @GetMapping("/{id}")
    public ResponseEntity<NextGenStat> getStatById(@PathVariable Long id) {
        return ResponseEntity.ok(nextGenStatService.getStatById(id));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<NextGenStat>> getStatsByPlayer(
            @PathVariable Long playerId,
            @RequestParam Integer season) {
        return ResponseEntity.ok(nextGenStatService.getStatsByPlayerAndSeason(playerId, season));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<NextGenStat>> getStatsByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(nextGenStatService.getStatsByGame(gameId));
    }
}

package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.SnapCount;
import nflanalytics.repository.SnapCountRepository;

@RestController
@RequestMapping("/api/snap-counts")
@RequiredArgsConstructor
public class SnapCountController {

    private final SnapCountRepository snapCountRepository;

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<SnapCount>> getSnapCountsByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(snapCountRepository.findByGame_Id(gameId));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<SnapCount>> getSnapCountsByPlayer(
            @PathVariable Long playerId,
            @RequestParam Integer season) {
        return ResponseEntity.ok(snapCountRepository.findByPlayer_IdAndSeasonOrderByWeek(playerId, season));
    }
}

package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.service.PlayerAvailabilityService;
import nflanalytics.service.PlayerAvailabilityService.InjuryAvailability;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerAvailabilityController {
    private final PlayerAvailabilityService availabilityService;

    @GetMapping("/{playerId}/games-missed")
    public ResponseEntity<Integer> getGamesMissed(
            @PathVariable Long playerId,
            @RequestParam Integer season) {
        return ResponseEntity.ok(availabilityService.countGamesMissedDueToInjury(playerId, season));
    }

    @GetMapping("/{playerId}/played-despite-injury")
    public ResponseEntity<Boolean> playedDespiteInjury(
            @PathVariable Long playerId,
            @RequestParam Long gameId) {
        return ResponseEntity.ok(availabilityService.playedDespiteInjury(playerId, gameId));
    }

    @GetMapping("/{playerId}/availability")
    public ResponseEntity<List<InjuryAvailability>> getAvailabilityTimeline(
            @PathVariable Long playerId,
            @RequestParam Integer season) {
        return ResponseEntity.ok(availabilityService.getAvailabilityTimeline(playerId, season));
    }
}

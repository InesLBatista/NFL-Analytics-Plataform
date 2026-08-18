package nflanalytics.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.service.PlayerAvailabilityService;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class PlayerAvailabilityController {
    private final PlayerAvailabilityService availabilityService;

    @GetMapping("/{playerId}/games-missed")
    public int getGamesMissed(@PathVariable Long playerId, @RequestParam Integer season) {
        return availabilityService.countGamesMissedDueToInjury(playerId, season);
    }
}

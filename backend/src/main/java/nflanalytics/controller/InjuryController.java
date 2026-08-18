package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Injury;
import nflanalytics.service.InjuryService;

@RestController
@RequestMapping("/api/injuries")
@RequiredArgsConstructor
public class InjuryController {

    private final InjuryService injuryService;

    @GetMapping("/{id}")
    public ResponseEntity<Injury> getInjuryById(@PathVariable Long id) {
        return ResponseEntity.ok(injuryService.getInjuryById(id));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Injury>> getInjuriesByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(injuryService.getInjuriesByGame(gameId));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<Injury>> getInjuryHistory(
            @PathVariable Long playerId,
            @RequestParam Integer season) {
        return ResponseEntity.ok(injuryService.getInjuryHistoryByPlayer(playerId, season));
    }
}

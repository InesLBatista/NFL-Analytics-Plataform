package nflanalytics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.service.NflverseImportService;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final NflverseImportService importService;

    @PostMapping("/teams")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importTeams() {
        try {
            importService.importTeams();
            return ResponseEntity.ok("Teams imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import teams: " + e.getMessage());
        }
    }

    @PostMapping("/games/{fromSeason}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importGames(@PathVariable int fromSeason) {
        try {
            importService.importGames(fromSeason);
            return ResponseEntity.ok("Games imported from season " + fromSeason);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import games: " + e.getMessage());
        }
    }

    @PostMapping("/players")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importPlayers() {
        try {
            importService.importPlayers();
            return ResponseEntity.ok("Players imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import players: " + e.getMessage());
        }
    }

    @PostMapping("/player-stats/{season}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importPlayerStats(@PathVariable int season) {
        try {
            importService.importPlayerStats(season);
            return ResponseEntity.ok("Player stats imported for season " + season);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import player stats: " + e.getMessage());
        }
    }

    @PostMapping("/game-stats/{season}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importGameStats(@PathVariable int season) {
        try {
            importService.importGameStats(season);
            return ResponseEntity.ok("Game stats imported for season " + season);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import game stats: " + e.getMessage());
        }
    }
}

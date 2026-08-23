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

    @PostMapping("/officials/{season}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importOfficials(@PathVariable int season) {
        try {
            importService.importOfficials(season);
            return ResponseEntity.ok("Officials imported for season " + season);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import officials: " + e.getMessage());
        }
    }

    @PostMapping("/draft-picks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importDraftPicks() {
        try {
            importService.importDraftPicks();
            return ResponseEntity.ok("Draft picks imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import draft picks: " + e.getMessage());
        }
    }

    @PostMapping("/injuries/{season}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importInjuries(@PathVariable int season) {
        try {
            importService.importInjuries(season);
            return ResponseEntity.ok("Injuries imported for season " + season);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import injuries: " + e.getMessage());
        }
    }

    @PostMapping("/snap-counts/{season}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importSnapCounts(@PathVariable int season) {
        try {
            importService.importSnapCounts(season);
            return ResponseEntity.ok("Snap counts imported for season " + season);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import snap counts: " + e.getMessage());
        }
    }

    @PostMapping("/contracts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importContracts() {
        try {
            importService.importContracts();
            return ResponseEntity.ok("Contracts imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import contracts: " + e.getMessage());
        }
    }

    // ngs is split into 3 separate endpoints matching the 3 csv files on nflverse (passing, rushing, receiving)
    @PostMapping("/next-gen-stats/passing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importNextGenStatsPassing() {
        try {
            importService.importNextGenStats("passing");
            return ResponseEntity.ok("Next gen stats (passing) imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import next gen stats (passing): " + e.getMessage());
        }
    }

    @PostMapping("/next-gen-stats/rushing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importNextGenStatsRushing() {
        try {
            importService.importNextGenStats("rushing");
            return ResponseEntity.ok("Next gen stats (rushing) imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import next gen stats (rushing): " + e.getMessage());
        }
    }

    @PostMapping("/next-gen-stats/receiving")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importNextGenStatsReceiving() {
        try {
            importService.importNextGenStats("receiving");
            return ResponseEntity.ok("Next gen stats (receiving) imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import next gen stats (receiving): " + e.getMessage());
        }
    }

    @PostMapping("/depth-charts/{season}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importDepthCharts(@PathVariable int season) {
        try {
            importService.importDepthCharts(season);
            return ResponseEntity.ok("Depth charts imported for season " + season);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import depth charts: " + e.getMessage());
        }
    }

    @PostMapping("/trades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importTrades() {
        try {
            importService.importTrades();
            return ResponseEntity.ok("Trades imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import trades: " + e.getMessage());
        }
    }

    @PostMapping("/combine")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importCombine() {
        try {
            importService.importCombine();
            return ResponseEntity.ok("Combine results imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import combine results: " + e.getMessage());
        }
    }

    @PostMapping("/ftn-charting/{season}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importFtnCharting(@PathVariable int season) {
        try {
            importService.importFtnCharting(season);
            return ResponseEntity.ok("FTN charting imported for season " + season);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to import FTN charting: " + e.getMessage());
        }
    }
}

package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Game;
import nflanalytics.service.GameService;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }

    @GetMapping("/season/{season}")
    public ResponseEntity<List<Game>> getGamesBySeason(@PathVariable Integer season) {
        return ResponseEntity.ok(gameService.getGamesBySeason(season));
    }

    @GetMapping("/season/{season}/week/{week}")
    public ResponseEntity<List<Game>> getGamesBySeasonAndWeek(
            @PathVariable Integer season,
            @PathVariable Integer week) {
        return ResponseEntity.ok(gameService.getGamesBySeasonAndWeek(season, week));
    }

    @GetMapping("/team/{abbreviation}")
    public ResponseEntity<List<Game>> getTeamSeasonGames(
            @PathVariable String abbreviation,
            @RequestParam Integer season) {
        return ResponseEntity.ok(gameService.getTeamSeasonGames(season, abbreviation));
    }
}

package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Player;
import nflanalytics.service.PlayerService;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Player>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(playerService.searchPlayersByName(name));
    }

    @GetMapping("/team/{abbreviation}")
    public ResponseEntity<List<Player>> getPlayersByTeam(@PathVariable String abbreviation) {
        return ResponseEntity.ok(playerService.getPlayersByTeam(abbreviation));
    }
}

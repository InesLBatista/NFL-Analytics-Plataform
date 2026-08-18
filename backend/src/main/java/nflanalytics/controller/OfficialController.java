package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Official;
import nflanalytics.service.OfficialService;

@RestController
@RequestMapping("/api/officials")
@RequiredArgsConstructor
public class OfficialController {

    private final OfficialService officialService;

    @GetMapping("/{id}")
    public ResponseEntity<Official> getOfficialById(@PathVariable Long id) {
        return ResponseEntity.ok(officialService.getOfficialById(id));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Official>> getOfficialsByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(officialService.getOfficialsByGame(gameId));
    }
}

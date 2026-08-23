package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.CombineResult;
import nflanalytics.service.CombineResultService;

@RestController
@RequestMapping("/api/combine")
@RequiredArgsConstructor
public class CombineResultController {

    private final CombineResultService combineResultService;

    @GetMapping("/{id}")
    public ResponseEntity<CombineResult> getResultById(@PathVariable Long id) {
        return ResponseEntity.ok(combineResultService.getResultById(id));
    }

    @GetMapping("/season/{season}")
    public ResponseEntity<List<CombineResult>> getResultsBySeason(@PathVariable Integer season) {
        return ResponseEntity.ok(combineResultService.getResultsBySeason(season));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<CombineResult>> getResultsByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(combineResultService.getResultsByPlayer(playerId));
    }
}

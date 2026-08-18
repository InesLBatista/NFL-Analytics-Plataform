package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DraftPick;
import nflanalytics.service.DraftPickService;

@RestController
@RequestMapping("/api/draft-picks")
@RequiredArgsConstructor
public class DraftPickController {

    private final DraftPickService draftPickService;

    @GetMapping("/{id}")
    public ResponseEntity<DraftPick> getPickById(@PathVariable Long id) {
        return ResponseEntity.ok(draftPickService.getPickById(id));
    }

    @GetMapping("/season/{season}")
    public ResponseEntity<List<DraftPick>> getPicksBySeason(@PathVariable Integer season) {
        return ResponseEntity.ok(draftPickService.getPicksBySeason(season));
    }
}

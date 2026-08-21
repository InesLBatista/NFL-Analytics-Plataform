package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DepthChartEntry;
import nflanalytics.service.DepthChartService;

@RestController
@RequestMapping("/api/depth-charts")
@RequiredArgsConstructor
public class DepthChartController {

    private final DepthChartService depthChartService;

    @GetMapping("/{id}")
    public ResponseEntity<DepthChartEntry> getEntryById(@PathVariable Long id) {
        return ResponseEntity.ok(depthChartService.getEntryById(id));
    }

    @GetMapping("/{team}")
    public ResponseEntity<List<DepthChartEntry>> getDepthChart(
            @PathVariable String team,
            @RequestParam Integer season,
            @RequestParam Integer week) {
        return ResponseEntity.ok(depthChartService.getDepthChart(team, season, week));
    }

    @GetMapping("/{team}/starter")
    public ResponseEntity<DepthChartEntry> getStarter(
            @PathVariable String team,
            @RequestParam Integer season,
            @RequestParam Integer week,
            @RequestParam String position) {
        return ResponseEntity.ok(depthChartService.getStarter(team, season, week, position));
    }
}

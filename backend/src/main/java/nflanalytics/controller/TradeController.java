package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Trade;
import nflanalytics.service.TradeService;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/{id}")
    public ResponseEntity<Trade> getTradeById(@PathVariable Long id) {
        return ResponseEntity.ok(tradeService.getTradeById(id));
    }

    @GetMapping("/season/{season}")
    public ResponseEntity<List<Trade>> getTradesBySeason(@PathVariable Integer season) {
        return ResponseEntity.ok(tradeService.getTradesBySeason(season));
    }

    @GetMapping("/team/{abbreviation}")
    public ResponseEntity<List<Trade>> getByTeamAndSeason(@PathVariable String abbreviation, @RequestParam Integer season) {
        return ResponseEntity.ok(tradeService.getByTeamAndSeason(abbreviation, season));
    }
}

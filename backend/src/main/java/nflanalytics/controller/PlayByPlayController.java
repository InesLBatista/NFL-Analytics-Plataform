package nflanalytics.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.PlayByPlay;
import nflanalytics.service.PlayByPlayService;

@RestController
@RequestMapping("/api/plays")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class PlayByPlayController {
    private final PlayByPlayService playByPlayService;

    @GetMapping("/game/{gameId}")
    public List<PlayByPlay> getPlaysByGame(@PathVariable Long gameId) {
        return playByPlayService.getPlaysByGame(gameId);
    }   
}
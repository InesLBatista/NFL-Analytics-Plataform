package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Game;
import nflanalytics.repository.GameRepository;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game getGameById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + id));
    }

    public List<Game> getGamesBySeason(Integer season) {
        return gameRepository.findBySeason(season);
    }

    public List<Game> getGamesBySeasonAndWeek(Integer season, Integer week) {
        return gameRepository.findBySeasonAndWeek(season, week);
    }

    // todos os jogos (casa + fora) de uma equipa numa época
    public List<Game> getTeamSeasonGames(Integer season, String abbreviation) {
        return gameRepository.findTeamSeasonGames(season, abbreviation);
    }
}

package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.GameStats;
import nflanalytics.repository.GameStatsRepository;

@Service
@RequiredArgsConstructor
public class GameStatsService {

    private final GameStatsRepository gameStatsRepository;

    public List<GameStats> getStatsByGame(Long gameId) {
        return gameStatsRepository.findByGame_Id(gameId);
    }

    public GameStats getStatsById(Long id) {
        return gameStatsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GameStats not found with id: " + id));
    }
}

package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.PlayerStats;
import nflanalytics.repository.PlayerStatsRepository;

@Service
@RequiredArgsConstructor
public class PlayerStatsService {

    private final PlayerStatsRepository playerStatsRepository;

    public List<PlayerStats> getStatsByPlayer(Long playerId) {
        return playerStatsRepository.findByPlayer_Id(playerId);
    }

    public List<PlayerStats> getStatsByGame(Long gameId) {
        return playerStatsRepository.findByGame_Id(gameId);
    }

    public PlayerStats getStatsById(Long id) {
        return playerStatsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlayerStats not found with id: " + id));
    }
}

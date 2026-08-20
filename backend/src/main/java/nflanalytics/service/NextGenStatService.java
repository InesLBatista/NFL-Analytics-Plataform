package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.NextGenStat;
import nflanalytics.repository.NextGenStatRepository;

@Service
@RequiredArgsConstructor
public class NextGenStatService {

    private final NextGenStatRepository nextGenStatRepository;

    //all ngs records for a player in a season, ordered by week
    public List<NextGenStat> getStatsByPlayerAndSeason(Long playerId, Integer season) {
        return nextGenStatRepository.findByPlayer_IdAndSeasonOrderByWeek(playerId, season);
    }

    //all ngs records linked to a specific game (passing, rushing and receiving combined)
    public List<NextGenStat> getStatsByGame(Long gameId) {
        return nextGenStatRepository.findByGame_Id(gameId);
    }

    public NextGenStat getStatById(Long id) {
        return nextGenStatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NextGenStat not found with id: " + id));
    }
}

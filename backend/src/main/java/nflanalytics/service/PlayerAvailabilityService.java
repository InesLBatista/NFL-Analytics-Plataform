//here im calculating the real availability for a player with the fussing of Injury (reported) and PlayerStats (game described) 

//player "Out" on the report and with no stats on that game confirms it has not played
//player "Questionable" with stats on that game confirms it has been played despite the injury

package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Injury;
import nflanalytics.model.PlayerStats;
import nflanalytics.repository.InjuryRepository;
import nflanalytics.repository.PlayerStatsRepository;

@Service
@RequiredArgsConstructor
public class PlayerAvailabilityService {
    private final InjuryRepository injuryRepository;
    private final PlayerStatsRepository playerStatsRepository;

    //number of games missed on a season cause of injury
    public int countGamesMissedDueToInjury(Long playerId, Integer season) {
        List<Injury> history = injuryRepository.findByPlayer_IdAndSeasonOrderByWeek(playerId, season);

        return (int) history.stream()
                .filter(injury -> "Out".equalsIgnoreCase(injury.getReportStatus()))
                .filter(injury -> injury.getGame() != null)
                .filter(injury -> !hasPlayerStatsForGame(playerId, injury.getGame().getId()))
                .count();
    }

    //if a player, despite the injury on the report, played
    public boolean playedDespiteInjury(Long playerId, Long gameId) {
        return hasPlayerStatsForGame(playerId, gameId);
    }


    
    private boolean hasPlayerStatsForGame(Long playerId, Long gameId) {
        List<PlayerStats> stats = playerStatsRepository.findByGame_Id(gameId);
        return stats.stream().anyMatch(ps -> ps.getPlayer().getId().equals(playerId));
    }

}

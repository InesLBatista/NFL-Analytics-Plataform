//here im calculating the real availability for a player with the fussing of Injury (reported) and PlayerStats (game described) 

//player "Out" on the report and with no stats on that game confirms it has not played
//player "Questionable" with stats on that game confirms it has been played despite the injury

package nflanalytics.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DepthChartEntry;
import nflanalytics.model.Injury;
import nflanalytics.repository.DepthChartRepository;
import nflanalytics.repository.InjuryRepository;
import nflanalytics.repository.PlayerStatsRepository;

@Service
@RequiredArgsConstructor
public class PlayerAvailabilityService {
    private final InjuryRepository injuryRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final DepthChartRepository depthChartRepository;

    //number of games missed on a season cause of injury
    public int countGamesMissedDueToInjury(Long playerId, Integer season) {
        List<Injury> history = injuryRepository.findByPlayer_IdAndSeasonOrderByWeek(playerId, season);

        //every game_id that a player has stats in a single query
        //changed to avoid N+1
        Set<Long> gamesWithStats = playerStatsRepository.findByPlayer_Id(playerId).stream()
                .map(ps -> ps.getGame().getId())
                .collect(Collectors.toSet());

        return (int) history.stream()
                .filter(injury -> "Out".equalsIgnoreCase(injury.getReportStatus()))
                .filter(injury -> injury.getGame() != null)
                .filter(injury -> !gamesWithStats.contains(injury.getGame().getId()))
                .count();
    }

    //if a player, despite the injury on the report, played
    public boolean playedDespiteInjury(Long playerId, Long gameId) {
        return playerStatsRepository.existsByPlayer_IdAndGame_Id(playerId, gameId);
    }

    //returns injury's history of a single playes with a flag of participation in every game
    //to use in the frontend building timeline of availibily 
    public List<InjuryAvailability> getAvailabilityTimeline(Long playerId, Integer season) {
        List<Injury> history = injuryRepository.findByPlayer_IdAndSeasonOrderByWeek(playerId, season);

        Set<Long> gamesWithStats = playerStatsRepository.findByPlayer_Id(playerId).stream()
                .map(ps -> ps.getGame().getId())
                .collect(Collectors.toSet());

        return history.stream()
                .map(injury -> new InjuryAvailability(
                        injury,
                        injury.getGame() != null && gamesWithStats.contains(injury.getGame().getId())
                ))
                .collect(Collectors.toList());
    }


    public record InjuryAvailability(Injury injury, boolean actuallyPlayed) {}


    public DepthChartEntry findLikelyReplacement(String team, Integer season, Integer week, String position, Long injuredPlayerId) {
        DepthChartEntry starter = depthChartRepository.findByTeamAndSeasonAndWeekAndPositionAndDepthRank( team, season, week, position, 1);

        //if starter is not injuried player, he played the game (substition not needed)
        if (starter != null && starter.getPlayer() != null && !starter.getPlayer().getId().equals(injuredPlayerId)) {
                return starter;
        }

        //otherwise injuried player is the starter
        return depthChartRepository.findByTeamAndSeasonAndWeekAndPositionAndDepthRank(team, season, week, position, 2);
        }
}

package nflanalytics.service;

import java.util.List;
import java.util.OptionalDouble;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.SnapCount;
import nflanalytics.repository.SnapCountRepository;

@Service
@RequiredArgsConstructor
public class PlayerUsageService {
    private final SnapCountRepository snapCountRepository;

    //average ofensive snaps in the season
    //gonna count more than 70% indicates starting player
    public Double getAverageOffenseSnapPct(Long playerId, Integer season) {
        List<SnapCount> history = snapCountRepository.findByPlayer_IdAndSeasonOrderByWeek(playerId, season);

        OptionalDouble avg = history.stream()
                .filter(sc -> sc.getOffensePct() != null)
                .mapToDouble(SnapCount::getOffensePct)
                .average();

        return avg.isPresent() ? avg.getAsDouble() : null;
    }


    //detects sharp week-over-week drops in usage (drop > 30 percentage points), as a possible sign of an unreported injury, loss of a starting role, or just a rest game
    public List<String> detectUsageDrops(Long playerId, Integer season) {
        List<SnapCount> history = snapCountRepository.findByPlayer_IdAndSeasonOrderByWeek(playerId, season);
        List<String> drops = new java.util.ArrayList<>();

        for (int i = 1; i < history.size(); i++) {
            SnapCount previous = history.get(i - 1);
            SnapCount current = history.get(i);

            if (previous.getOffensePct() == null || current.getOffensePct() == null) continue;

            double drop = previous.getOffensePct() - current.getOffensePct();
            if (drop > 30) {
                drops.add(current.getWeek() + " week" + ": drop of " + String.format("%.0f", drop) + " percentage points compared to the previous week (" + previous.getOffensePct() + "% → " + current.getOffensePct() + "%)");
            }
        }

        return drops;
    }
}

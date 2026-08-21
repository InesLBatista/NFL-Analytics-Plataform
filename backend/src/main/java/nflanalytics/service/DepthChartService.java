package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DepthChartEntry;
import nflanalytics.repository.DepthChartRepository;

@Service
@RequiredArgsConstructor
public class DepthChartService {
    private final DepthChartRepository depthChartRepository;

    //full depth chart for a team in a specific week
    public List<DepthChartEntry> getDepthChart(String team, Integer season, Integer week) {
        return depthChartRepository.findByTeamAndSeasonAndWeek(team, season, week);
    }

    //starter at a specific position for a given team and week (depthRank == 1)
    public DepthChartEntry getStarter(String team, Integer season, Integer week, String position) {
        return depthChartRepository.findByTeamAndSeasonAndWeekAndPositionAndDepthRank(team, season, week, position, 1);
    }

    public DepthChartEntry getEntryById(Long id) {
        return depthChartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DepthChartEntry not found with id: " + id));
    }
}

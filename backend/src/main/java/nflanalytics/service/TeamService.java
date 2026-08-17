package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Team;
import nflanalytics.repository.TeamRepository;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
    }

    public Team getTeamByAbbreviation(String abbreviation) {
        Team team = teamRepository.findByAbbreviation(abbreviation);
        if (team == null) throw new RuntimeException("Team not found with abbreviation: " + abbreviation);
        return team;
    }
}

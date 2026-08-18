package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Injury;
import nflanalytics.repository.InjuryRepository;

@Service
@RequiredArgsConstructor
public class InjuryService {

    private final InjuryRepository injuryRepository;

    public List<Injury> getInjuriesByGame(Long gameId) {
        return injuryRepository.findByGame_Id(gameId);
    }

    public List<Injury> getInjuryHistoryByPlayer(Long playerId, Integer season) {
        return injuryRepository.findByPlayer_IdAndSeasonOrderByWeek(playerId, season);
    }

    public Injury getInjuryById(Long id) {
        return injuryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Injury not found with id: " + id));
    }
}

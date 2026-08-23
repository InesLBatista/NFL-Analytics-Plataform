package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.CombineResult;
import nflanalytics.repository.CombineResultRepository;

@Service
@RequiredArgsConstructor
public class CombineResultService {

    private final CombineResultRepository combineResultRepository;

    public List<CombineResult> getResultsBySeason(Integer season) {
        return combineResultRepository.findBySeason(season);
    }

    public List<CombineResult> getResultsByPlayer(Long playerId) {
        return combineResultRepository.findByPlayer_Id(playerId);
    }

    public CombineResult getResultById(Long id) {
        return combineResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CombineResult not found with id: " + id));
    }
}

package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.FtnCharting;
import nflanalytics.repository.FtnChartingRepository;

@Service
@RequiredArgsConstructor
public class FtnChartingService {

    private final FtnChartingRepository ftnChartingRepository;

    public List<FtnCharting> getChartingByGame(Long gameId) {
        return ftnChartingRepository.findByGame_Id(gameId);
    }

    public FtnCharting getChartingById(Long id) {
        return ftnChartingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FtnCharting not found with id: " + id));
    }
}

package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DraftPick;
import nflanalytics.repository.DraftPickRepository;

@Service
@RequiredArgsConstructor
public class DraftPickService {

    private final DraftPickRepository draftPickRepository;

    public List<DraftPick> getPicksBySeason(Integer season) {
        return draftPickRepository.findBySeason(season);
    }

    public DraftPick getPickById(Long id) {
        return draftPickRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DraftPick not found with id: " + id));
    }
}

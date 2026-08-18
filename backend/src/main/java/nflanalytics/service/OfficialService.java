package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Official;
import nflanalytics.repository.OfficialRepository;

@Service
@RequiredArgsConstructor
public class OfficialService {

    private final OfficialRepository officialRepository;


    public List<Official> getOfficialsByGame(Long gameId) {
        return officialRepository.findByGame_Id(gameId);
    }

    public Official getOfficialById(Long id) {
        return officialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Official not found with id: " + id));
    }
}

package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.PlayByPlay;
import nflanalytics.repository.PlayByPlayRepository;

@Service
@RequiredArgsConstructor
public class PlayByPlayService {
    private final PlayByPlayRepository playByPlayRepository;

    public List<PlayByPlay> getPlaysByGame(Long gameId) {
        return playByPlayRepository.findByGame_IdOrderById(gameId);
    }
}


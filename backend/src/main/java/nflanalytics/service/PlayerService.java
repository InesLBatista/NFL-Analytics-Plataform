package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Player;
import nflanalytics.repository.PlayerRepository;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }

    public List<Player> searchPlayersByName(String name) {
        return playerRepository.findByFullNameContainingIgnoreCase(name);
    }

    // todos os jogadores de uma equipa pela sigla
    public List<Player> getPlayersByTeam(String abbreviation) {
        return playerRepository.findByTeam_Abbreviation(abbreviation);
    }
}

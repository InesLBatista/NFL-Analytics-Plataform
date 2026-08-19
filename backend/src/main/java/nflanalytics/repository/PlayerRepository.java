package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByExternalId(String externalId);
    List<Player> findByFullNameContainingIgnoreCase(String name);
    List<Player> findByTeam_Abbreviation(String abbreviation);

    Player findByFullNameIgnoreCaseAndTeam_Abbreviation(String fullName, String teamAbbreviation);
}

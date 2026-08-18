package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.Official;

public interface OfficialRepository extends JpaRepository<Official, Long> {
    List<Official> findByGame_Id(Long gameId);
    boolean existsByGame_IdAndNameAndRole(Long gameId, String name, String role);
}

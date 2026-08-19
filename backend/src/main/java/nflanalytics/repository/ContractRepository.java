package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.Contract;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    boolean existsByPlayerNameAndYearSigned(String playerName, Integer yearSigned);

    List<Contract> findByPlayer_IdOrderByYearSignedDesc(Long playerId);
    List<Contract> findByPlayer_IdAndIsActiveTrue(Long playerId);
}

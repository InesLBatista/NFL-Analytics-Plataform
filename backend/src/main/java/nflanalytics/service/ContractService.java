package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Contract;
import nflanalytics.repository.ContractRepository;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;

    public List<Contract> getContractsByPlayer(Long playerId) {
        return contractRepository.findByPlayer_IdOrderByYearSignedDesc(playerId);
    }

    public List<Contract> getActiveContractsByPlayer(Long playerId) {
        return contractRepository.findByPlayer_IdAndIsActiveTrue(playerId);
    }

    public Contract getContractById(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found with id: " + id));
    }
}

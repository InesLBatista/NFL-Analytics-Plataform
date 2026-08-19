package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Contract;
import nflanalytics.service.ContractService;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<Contract>> getContractsByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(contractService.getContractsByPlayer(playerId));
    }

    @GetMapping("/player/{playerId}/active")
    public ResponseEntity<List<Contract>> getActiveContractsByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(contractService.getActiveContractsByPlayer(playerId));
    }
}

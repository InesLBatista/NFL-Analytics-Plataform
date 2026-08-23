package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Trade;
import nflanalytics.repository.TradeRepository;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;

    //all trades recorded in a given season
    public List<Trade> getTradesBySeason(Integer season) {
        return tradeRepository.findBySeason(season);
    }

    public Trade getTradeById(Long id) {
        return tradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trade not found with id: " + id));
    }
}

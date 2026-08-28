package nflanalytics.repository;

import nflanalytics.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    boolean existsBySeasonAndTeamGivingAndAssetDescription(Integer season, String teamGiving, String assetDescription);
    List<Trade> findBySeason(Integer season);

    List<Trade> findBySeasonAndTeamGivingOrSeasonAndTeamReceiving(Integer season1, String teamGiving, Integer season2, String teamReceiving);
}
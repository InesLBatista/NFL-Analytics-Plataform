package nflanalytics.service;

import nflanalytics.model.Game;
import nflanalytics.model.GameStats;
import nflanalytics.model.Team;
import nflanalytics.model.DraftPick;
import nflanalytics.model.Trade;
import nflanalytics.repository.GameRepository;
import nflanalytics.repository.GameStatsRepository;
import nflanalytics.repository.InjuryRepository;
import nflanalytics.repository.DraftPickRepository;
import nflanalytics.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TeamSummaryService {
    private final GameRepository gameRepository;
    private final GameStatsRepository gameStatsRepository;
    private final InjuryRepository injuryRepository;
    private final DraftPickRepository draftPickRepository;
    private final TradeRepository tradeRepository;

    public String buildSeasonSummary(Team team, Integer season) {
        System.out.println("Building season summary for team: " + team.getName() +  " (" + team.getAbbreviation() + "), season: " + season);
        
        StringBuilder sb = new StringBuilder();


        sb.append(team.getName()).append(" (").append(team.getAbbreviation()).append(") -- ")
          .append(season).append(" season summary.\n\n");

        System.out.println("Fetching games for team: " + team.getAbbreviation());
        List<Game> games = gameRepository.findTeamSeasonGames(season, team.getAbbreviation());
        System.out.println("Total games found: " + games.size());
        

        int wins = 0, losses = 0, ties = 0;
        String headCoach = null;
        int gamesProcessed = 0;

        for (Game g : games) {
            if (g.getStatus() != Game.GameStatus.FINAL) {
                System.out.println("Skipping non-final game: " + g.getId());
                continue;
            }

            boolean isHome = g.getHomeTeam().getId().equals(team.getId());
            Integer teamScore = isHome ? g.getHomeScore() : g.getAwayScore();
            Integer oppScore = isHome ? g.getAwayScore() : g.getHomeScore();

            if (teamScore == null || oppScore == null) {
                System.out.println("Skipping game with null scores: " + g.getId());
                continue;
            }

            gamesProcessed++;
            System.out.println("Processing game #" + gamesProcessed + " - Team score: " + teamScore + ", Opponent score: " + oppScore);

            if (teamScore > oppScore) wins++;
            else if (teamScore < oppScore) losses++;
            else ties++;

            headCoach = isHome ? g.getHomeCoach() : g.getAwayCoach();
        }

        System.out.println("Record calculated - Wins: " + wins + ", Losses: " + losses + ", Ties: " + ties);
        System.out.println("Head coach: " + (headCoach != null ? headCoach : "N/A"));



        sb.append("Record: ").append(wins).append(" wins, ").append(losses).append(" losses");
        if (ties > 0) sb.append(", ").append(ties).append(" ties");
        sb.append(".\n");

        if (headCoach != null) {
            sb.append("Head coach: ").append(headCoach).append(".\n");
        }


        System.out.println("Fetching season stats for team ID: " + team.getId());
        List<GameStats> seasonStats = gameStatsRepository.findByTeam_IdAndGame_Season(team.getId(), season);
        System.out.println("Season stats entries found: " + seasonStats.size());

        if (!seasonStats.isEmpty()) {
            System.out.println("Calculating average yards per game...");
            double avgYards = seasonStats.stream()
                    .filter(gs -> gs.getTotalYards() != null)
                    .mapToInt(GameStats::getTotalYards)
                    .average().orElse(0);
            System.out.println("Average total yards: " + String.format("%.0f", avgYards));

            System.out.println("Calculating average turnovers per game...");
            double avgTurnovers = seasonStats.stream()
                    .filter(gs -> gs.getTurnovers() != null)
                    .mapToInt(GameStats::getTurnovers)
                    .average().orElse(0);
            System.out.println("Average turnovers: " + String.format("%.1f", avgTurnovers));

            sb.append("Average total yards per game: ").append(String.format("%.0f", avgYards)).append(".\n");
            sb.append("Average turnovers per game: ").append(String.format("%.1f", avgTurnovers)).append(".\n");
        } else {
            System.out.println("No season stats available for this team");
        }



        System.out.println("Fetching draft picks for season " + season);
        List<DraftPick> picks = draftPickRepository.findBySeasonAndTeam_Id(season, team.getId());
        System.out.println("Draft picks found: " + picks.size());

        if (!picks.isEmpty()) {
            sb.append("Draft picks in ").append(season).append(": ");
            for (DraftPick p : picks) {
                sb.append(p.getPlayerName()).append(" (Round ").append(p.getRound())
                  .append(", Pick ").append(p.getPick()).append(", ").append(p.getPosition()).append("); ");
                System.out.println("  - " + p.getPlayerName() + " | Round: " + p.getRound() + ", Pick: " + p.getPick() + ", Position: " + p.getPosition());
            }
            sb.append("\n");
        } else {
            System.out.println("No draft picks found for this season");
        }



        System.out.println("Fetching trades for season " + season);
        List<Trade> trades = tradeRepository.findBySeasonAndTeamGivingOrSeasonAndTeamReceiving(
                season, team.getAbbreviation(), season, team.getAbbreviation());
        System.out.println("Trades found: " + trades.size());

        if (!trades.isEmpty()) {
            sb.append("Trades in ").append(season).append(": ");
            for (Trade t : trades) {
                boolean gave = t.getTeamGiving().equals(team.getAbbreviation());
                sb.append(gave ? "sent " : "received ").append(t.getAssetDescription())
                  .append(" (").append(gave ? t.getTeamReceiving() : t.getTeamGiving()).append("); ");
                System.out.println("  - " + (gave ? "Sent" : "Received") + " " + t.getAssetDescription() + 
                                  " " + (gave ? "to" : "from") + " " + (gave ? t.getTeamReceiving() : t.getTeamGiving()));
            }
            sb.append("\n");
        } else {
            System.out.println("No trades found for this season");
        }



        System.out.println("Fetching injury reports for team: " + team.getAbbreviation());
        long injuryReports = injuryRepository.countByTeamAndSeason(team.getAbbreviation(), season);
        System.out.println("Injury report entries found: " + injuryReports);

        if (injuryReports > 0) {
            sb.append("Total injury report entries during the season: ").append(injuryReports).append(".\n");
        } else {
            System.out.println("No injury report entries found");
        }


        String summary = sb.toString();
        System.out.println("Summary generation complete");
        System.out.println("Final summary length: " + summary.length() + " characters");
        System.out.println("Preview: " + summary.substring(0, Math.min(200, summary.length())) + "...");
        
        return summary;
    }
}
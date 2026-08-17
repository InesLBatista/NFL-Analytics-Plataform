package nflanalytics.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Game;
import nflanalytics.model.GameReport;
import nflanalytics.model.GameStats;
import nflanalytics.model.PlayByPlay;
import nflanalytics.model.PlayerStats;
import nflanalytics.repository.GameReportRepository;
import nflanalytics.repository.GameRepository;
import nflanalytics.repository.GameStatsRepository;
import nflanalytics.repository.PlayByPlayRepository;
import nflanalytics.repository.PlayerStatsRepository;

@Service
@RequiredArgsConstructor
public class GameReportService {

    private final GameRepository gameRepository;
    private final GameStatsRepository gameStatsRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final PlayByPlayRepository playByPlayRepository;
    private final GameReportRepository gameReportRepository;
    private final AnthropicClient anthropicClient;

    //retrieves an existing report for a specific game
    public GameReport getReport(Long gameId) {
        return gameReportRepository.findByGame_Id(gameId);
    }

    //generates the report of a specific game, aggregates data and builds the prompt
    public GameReport generateReport(Long gameId) throws Exception {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        String prompt = buildPrompt(game);
        String generatedText = anthropicClient.generateText(prompt);

        //if there's already a report on a certain game, not to duplicate
        GameReport report = gameReportRepository.findByGame_Id(gameId);
        if (report == null) {
            report = new GameReport();
            report.setGame(game);
        }
        report.setContent(generatedText);
        report.setGeneratedAt(LocalDateTime.now());

        return gameReportRepository.save(report);
    }



    private String buildPrompt(Game game) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a sports journalist specializing in NFL. ")
          .append("Write a game recap in Portuguese, with 3 to 4 paragraphs, ")
          .append("in a professional yet engaging tone, as if published on a sports website. ")
          .append("Use only the data provided below -- do not invent any statistics.\n\n");

        // Score and basic context
        sb.append("GAME: ").append(game.getAwayTeam().getName())
          .append(" @ ").append(game.getHomeTeam().getName())
          .append(" (Week ").append(game.getWeek()).append(", Season ").append(game.getSeason()).append(")\n");
        sb.append("FINAL SCORE: ").append(game.getAwayTeam().getAbbreviation()).append(" ")
          .append(game.getAwayScore()).append(" - ")
          .append(game.getHomeScore()).append(" ").append(game.getHomeTeam().getAbbreviation()).append("\n");

        if (game.getStadium() != null) {
            sb.append("Stadium: ").append(game.getStadium());
            if (game.getRoof() != null) sb.append(" (").append(game.getRoof()).append(")");
            sb.append("\n");
        }
        if (game.getHomeCoach() != null && game.getAwayCoach() != null) {
            sb.append("Coaches: ").append(game.getAwayCoach()).append(" vs ").append(game.getHomeCoach()).append("\n");
        }

        // Team statistics
        List<GameStats> teamStats = gameStatsRepository.findByGame_Id(game.getId());
        if (!teamStats.isEmpty()) {
            sb.append("\nTEAM STATISTICS:\n");
            for (GameStats gs : teamStats) {
                sb.append("- ").append(gs.getTeam().getAbbreviation())
                  .append(": ").append(gs.getTotalYards() != null ? gs.getTotalYards() : "N/A").append(" total yards, ")
                  .append(gs.getTurnovers() != null ? gs.getTurnovers() : "N/A").append(" turnovers\n");
            }
        }

        //top 5 players by total yards (passing + rushing + receiving)
        List<PlayerStats> allStats = playerStatsRepository.findByGame_Id(game.getId());
        List<PlayerStats> topPlayers = allStats.stream()
                .sorted(Comparator.comparingInt(this::totalYards).reversed())
                .limit(5)
                .collect(Collectors.toList());

        if (!topPlayers.isEmpty()) {
            sb.append("\nTOP PLAYERS OF THE GAME:\n");
            for (PlayerStats ps : topPlayers) {
                sb.append("- ").append(ps.getPlayer().getFullName()).append(" (").append(ps.getPlayer().getPosition()).append("): ");
                if (ps.getPassingYards() != null && ps.getPassingYards() > 0) {
                    sb.append(ps.getPassingYards()).append(" passing yards, ").append(nullToZero(ps.getPassingTouchdowns())).append(" TDs. ");
                }
                if (ps.getRushingYards() != null && ps.getRushingYards() > 0) {
                    sb.append(ps.getRushingYards()).append(" rushing yards, ").append(nullToZero(ps.getRushingTouchdowns())).append(" TDs. ");
                }
                if (ps.getReceivingYards() != null && ps.getReceivingYards() > 0) {
                    sb.append(ps.getReceptions()).append(" receptions, ").append(ps.getReceivingYards()).append(" yards. ");
                }
                sb.append("\n");
            }
        }

        //most decisive plays (greatest impact on win probability == EPA)
        List<PlayByPlay> plays = playByPlayRepository.findByGame_IdOrderById(game.getId());
        List<PlayByPlay> keyPlays = plays.stream()
                .filter(p -> p.getEpa() != null && p.getDescription() != null)
                .sorted(Comparator.comparingDouble((PlayByPlay p) -> Math.abs(p.getEpa())).reversed())
                .limit(6)
                .collect(Collectors.toList());

        if (!keyPlays.isEmpty()) {
            sb.append("\nMOST DECISIVE PLAYS (greatest impact on win probability):\n");
            for (PlayByPlay p : keyPlays) {
                sb.append("- ").append(p.getDescription()).append("\n");
            }
        }

        return sb.toString();
    }

    //calculates total yards for a player across all categories
    private int totalYards(PlayerStats ps) {
        return nullToZero(ps.getPassingYards()) + nullToZero(ps.getRushingYards()) + nullToZero(ps.getReceivingYards());
    }


    private int nullToZero(Integer value) {
        return value != null ? value : 0;
    }
}
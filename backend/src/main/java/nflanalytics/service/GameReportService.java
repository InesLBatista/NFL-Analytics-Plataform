package nflanalytics.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import nflanalytics.dto.WeekReportResult;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DepthChartEntry;
import nflanalytics.model.Game;
import nflanalytics.model.GameReport;
import nflanalytics.model.GameStats;
import nflanalytics.model.Injury;
import nflanalytics.model.NextGenStat;
import nflanalytics.model.Official;
import nflanalytics.model.PlayByPlay;
import nflanalytics.model.PlayerStats;
import nflanalytics.model.SnapCount;
import nflanalytics.repository.DepthChartRepository;
import nflanalytics.repository.GameReportRepository;
import nflanalytics.repository.GameRepository;
import nflanalytics.repository.GameStatsRepository;
import nflanalytics.repository.InjuryRepository;
import nflanalytics.repository.NextGenStatRepository;
import nflanalytics.repository.OfficialRepository;
import nflanalytics.repository.PlayByPlayRepository;
import nflanalytics.repository.PlayerStatsRepository;
import nflanalytics.repository.SnapCountRepository;

@Service
@RequiredArgsConstructor
public class GameReportService {

    private final GameRepository gameRepository;
    private final GameStatsRepository gameStatsRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final PlayByPlayRepository playByPlayRepository;
    private final InjuryRepository injuryRepository;
    private final OfficialRepository officialRepository;
    private final SnapCountRepository snapCountRepository;
    private final NextGenStatRepository nextGenStatRepository;
    private final DepthChartRepository depthChartRepository;
    private final GameReportRepository gameReportRepository;
    private final AnthropicClient anthropicClient;

    //retrieves an existing report for a specific game
    public GameReport getReport(Long gameId) {
        return gameReportRepository.findByGame_Id(gameId);
    }



    //processes each game independently so a single API failure does not abort the rest
    //a delay between calls avoids hitting Anthropic rate limits
    public WeekReportResult generateWeekReports(int season, int week) {
        List<Game> games = gameRepository.findBySeasonAndWeek(season, week);

        int generated = 0;
        int failed    = 0;
        int skipped   = 0;
        List<String> errors = new ArrayList<>();

        for (Game game : games) {
            //skip games that already have a report to avoid unnecessary API calls
            if (gameReportRepository.findByGame_Id(game.getId()) != null) {
                skipped++;
                continue;
            }

            try {
                String prompt = buildPrompt(game);
                String generatedText = anthropicClient.generateText(prompt);

                GameReport report = new GameReport();
                report.setGame(game);
                report.setContent(generatedText);
                report.setGeneratedAt(LocalDateTime.now());
                gameReportRepository.save(report);
                generated++;

                //small delay between calls to stay within API rate limits
                Thread.sleep(1_500);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                errors.add("Interrupted while processing game " + game.getId());
                break;
            } catch (Exception e) {
                //log the failure but continue with the next game
                failed++;
                errors.add(game.getHomeTeam().getAbbreviation() + " vs "
                        + game.getAwayTeam().getAbbreviation()
                        + " (id=" + game.getId() + "): " + e.getMessage());
            }
        }

        return new WeekReportResult(season, week, generated, failed, skipped, errors);
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
          .append("Write a game recap, with 3 to 4 paragraphs, ")
          .append("in a professional yet engaging tone, as if published on a sports website. ")
          .append("Use only the data provided below -- do not invent any statistics.\n\n");

        sb.append("Game: ").append(game.getAwayTeam().getName())
          .append(" @ ").append(game.getHomeTeam().getName())
          .append(" (Week ").append(game.getWeek()).append(", Season ").append(game.getSeason()).append(")\n");
        sb.append("Final score: ").append(game.getAwayTeam().getAbbreviation()).append(" ")
          .append(game.getAwayScore()).append(" - ")
          .append(game.getHomeScore()).append(" ").append(game.getHomeTeam().getAbbreviation()).append("\n");

        if (game.getStadium() != null) {
            sb.append("Stadium: ").append(game.getStadium());
            if (game.getRoof() != null) sb.append(" (").append(game.getRoof()).append(")");
            if (game.getSurface() != null) sb.append(", ").append(game.getSurface());
            sb.append("\n");
        }
        if (game.getTemp() != null) sb.append("Temperature: ").append(game.getTemp()).append("°F\n");
        if (game.getWind() != null) sb.append("Wind: ").append(game.getWind()).append(" mph\n");
        if (game.getHomeCoach() != null && game.getAwayCoach() != null) {
            sb.append("Coaches: ").append(game.getAwayCoach())
              .append(" (").append(game.getAwayTeam().getAbbreviation()).append(")")
              .append(" vs ").append(game.getHomeCoach())
              .append(" (").append(game.getHomeTeam().getAbbreviation()).append(")\n");
        }

        //match officials
        List<Official> officials = officialRepository.findByGame_Id(game.getId());
        if (!officials.isEmpty()) {
            sb.append("Match officials: ");
            sb.append(officials.stream()
                    .map(o -> o.getName() + " (" + o.getRole() + ")")
                    .collect(Collectors.joining(", ")));
            sb.append("\n");
        }

        // Team statistics
        List<GameStats> teamStats = gameStatsRepository.findByGame_Id(game.getId());
        if (!teamStats.isEmpty()) {
            sb.append("\nTeam statistics:\n");
            for (GameStats gs : teamStats) {
                sb.append("- ").append(gs.getTeam().getAbbreviation()).append(": ");
                if (gs.getTotalYards() != null)
                    sb.append(gs.getTotalYards()).append(" total yards (")
                      .append(nullToZero(gs.getPassingYards())).append(" passing, ")
                      .append(nullToZero(gs.getRushingYards())).append(" rushing), ");
                if (gs.getTurnovers() != null)
                    sb.append(gs.getTurnovers()).append(" turnovers, ");
                if (gs.getSacks() != null)
                    sb.append(gs.getSacks()).append(" sacks conceded, ");
                if (gs.getThirdDownConversions() != null && gs.getThirdDownAttempts() != null)
                    sb.append(gs.getThirdDownConversions()).append("/").append(gs.getThirdDownAttempts())
                      .append(" on 3rd down, ");
                if (gs.getPenalties() != null)
                    sb.append(gs.getPenalties()).append(" penalties (").append(nullToZero(gs.getPenaltyYards())).append(" yds)");
                sb.append("\n");
            }
        }

        //top 5 players by total yards (passing + rushing + receiving)
        List<PlayerStats> allStats = playerStatsRepository.findByGame_Id(game.getId());
        List<PlayerStats> topPlayers = allStats.stream()
                .sorted(Comparator.comparingInt(this::totalYards).reversed())
                .limit(5)
                .collect(Collectors.toList());

        if (!topPlayers.isEmpty()) {
            sb.append("\nTop players of the game:\n");
            for (PlayerStats ps : topPlayers) {
                sb.append("- ").append(ps.getPlayer().getFullName())
                  .append(" (").append(ps.getPlayer().getPosition()).append("): ");
                if (ps.getPassingYards() != null && ps.getPassingYards() > 0) {
                    sb.append(ps.getPassingCompletions()).append("/").append(ps.getPassingAttempts())
                      .append(", ").append(ps.getPassingYards()).append(" passing yds, ")
                      .append(nullToZero(ps.getPassingTouchdowns())).append(" TDs, ")
                      .append(nullToZero(ps.getInterceptions())).append(" INTs. ");
                }
                if (ps.getRushingYards() != null && ps.getRushingYards() > 0) {
                    sb.append(ps.getRushingAttempts()).append(" carries, ")
                      .append(ps.getRushingYards()).append(" rushing yds, ")
                      .append(nullToZero(ps.getRushingTouchdowns())).append(" TDs. ");
                }
                if (ps.getReceivingYards() != null && ps.getReceivingYards() > 0) {
                    sb.append(ps.getReceptions()).append("/").append(ps.getTargets())
                      .append(" rec, ").append(ps.getReceivingYards()).append(" yds, ")
                      .append(nullToZero(ps.getReceivingTouchdowns())).append(" TDs. ");
                }
                if (ps.getSacks() != null && ps.getSacks() > 0) {
                    sb.append(ps.getSacks()).append(" sacks. ");
                }

                //usage context from snap counts, when available for this player
                SnapCount snaps = findSnapCount(game, ps.getPlayer().getId());
                if (snaps != null && snaps.getOffensePct() != null) {
                    sb.append("Played ").append(String.format("%.0f", snaps.getOffensePct())).append("% of offensive snaps. ");
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
            sb.append("\nMost decisive plays (by EPA impact):\n");
            for (PlayByPlay p : keyPlays) {
                sb.append("- Q").append(p.getQuarter() != null ? p.getQuarter() : "?");
                if (p.getGameSecondsRemaining() != null) {
                    int mins = p.getGameSecondsRemaining() / 60;
                    int secs = p.getGameSecondsRemaining() % 60;
                    sb.append(" (").append(mins).append(":").append(String.format("%02d", secs)).append(" remaining)");
                }
                sb.append(": ").append(p.getDescription()).append("\n");
            }
        }

        //advanced metrics for the top players identified above
        if (!topPlayers.isEmpty()) {
            StringBuilder ngsSection = new StringBuilder();
            for (PlayerStats ps : topPlayers) {
                NextGenStat ngs = findNextGenStat(game, ps.getPlayer().getId());
                if (ngs == null) continue;

                ngsSection.append("- ").append(ps.getPlayer().getFullName()).append(" (").append(ngs.getStatType()).append("): ");

                if ("passing".equals(ngs.getStatType())) {
                    if (ngs.getAvgTimeToThrow() != null) ngsSection.append("avg time to throw ").append(ngs.getAvgTimeToThrow()).append("s, ");
                    if (ngs.getCompletionPctAboveExpectation() != null) ngsSection.append("completion % above expectation ").append(ngs.getCompletionPctAboveExpectation()).append(", ");
                    if (ngs.getAggressiveness() != null) ngsSection.append("aggressiveness ").append(ngs.getAggressiveness()).append("%. ");
                } else if ("rushing".equals(ngs.getStatType())) {
                    if (ngs.getRushYardsOverExpected() != null) ngsSection.append("rush yards over expected ").append(ngs.getRushYardsOverExpected()).append(", ");
                    if (ngs.getEfficiency() != null) ngsSection.append("efficiency ").append(ngs.getEfficiency()).append(". ");
                } else if ("receiving".equals(ngs.getStatType())) {
                    if (ngs.getAvgSeparation() != null) ngsSection.append("avg separation ").append(ngs.getAvgSeparation()).append(" yds, ");
                    if (ngs.getAvgYacAboveExpectation() != null) ngsSection.append("YAC above expectation ").append(ngs.getAvgYacAboveExpectation()).append(". ");
                }

                ngsSection.append("\n");
            }

            if (ngsSection.length() > 0) {
                sb.append("\nAdvanced metrics (Next Gen Stats):\n").append(ngsSection);
            }
        }

        //injuries, cross-referenced with who actually played and who likely replaced them
        List<Injury> injuries = injuryRepository.findByGame_Id(game.getId());
        if (!injuries.isEmpty()) {
            //players confirmed out of the game
            List<Injury> out = injuries.stream()
                    .filter(i -> "Out".equalsIgnoreCase(i.getReportStatus()))
                    .collect(Collectors.toList());

            //players with uncertain or limited status
            List<Injury> limited = injuries.stream()
                    .filter(i -> i.getReportStatus() != null
                            && !i.getReportStatus().equalsIgnoreCase("Out")
                            && !i.getReportStatus().equalsIgnoreCase("Full"))
                    .collect(Collectors.toList());

            if (!out.isEmpty()) {
                sb.append("\nPlayers ruled out:\n");
                for (Injury i : out) {
                    sb.append("- ");
                    if (i.getPlayer() != null) sb.append(i.getPlayer().getFullName());
                    else sb.append("Unknown player");
                    sb.append(" (").append(i.getTeam()).append(")");
                    if (i.getReportPrimaryInjury() != null)
                        sb.append(" — ").append(i.getReportPrimaryInjury());
                    if (i.getReportSecondaryInjury() != null)
                        sb.append(", ").append(i.getReportSecondaryInjury());

                    //tries to identify the likely replacement via the depth chart
                    if (i.getPlayer() != null) {
                        DepthChartEntry replacement = findLikelyReplacement(game, i.getTeam(), i.getPlayer());
                        if (replacement != null && replacement.getPlayer() != null) {
                            sb.append(" -- likely replaced by ").append(replacement.getPlayer().getFullName());
                        }
                    }
                    sb.append("\n");
                }
            }

            if (!limited.isEmpty()) {
                sb.append("\nInjured but listed (questionable/doubtful/limited):\n");
                for (Injury i : limited) {
                    sb.append("- ");
                    if (i.getPlayer() != null) sb.append(i.getPlayer().getFullName());
                    else sb.append("Unknown player");
                    sb.append(" (").append(i.getTeam()).append(")")
                      .append(" — ").append(i.getReportStatus());
                    if (i.getReportPrimaryInjury() != null)
                        sb.append(", ").append(i.getReportPrimaryInjury());
                    if (i.getPracticeStatus() != null)
                        sb.append(" [practice: ").append(i.getPracticeStatus()).append("]");
                    sb.append("\n");
                }
            }
        }

        sb.append("\nWrite the recap now:");
        return sb.toString();
    }

    //finds this player's snap count entry for this specific game, if imported
    private SnapCount findSnapCount(Game game, Long playerId) {
        return snapCountRepository.findByGame_Id(game.getId()).stream()
                .filter(sc -> sc.getPlayer() != null && sc.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    //finds this player's Next Gen Stats entry for this specific game, if imported
    private NextGenStat findNextGenStat(Game game, Long playerId) {
        return nextGenStatRepository.findByGame_Id(game.getId()).stream()
                .filter(ngs -> ngs.getPlayer() != null && ngs.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    //finds who most likely took over an injured player's snaps, using the depth chart:
    //if the injured player was the starter (depthRank=1), the replacement is depthRank=2, otherwise, the current starter at that position is assumed to have played instead
    private DepthChartEntry findLikelyReplacement(Game game, String team, nflanalytics.model.Player injuredPlayer) {
        DepthChartEntry starter = depthChartRepository.findByTeamAndSeasonAndWeekAndPositionAndDepthRank(
                team, game.getSeason(), game.getWeek(), injuredPlayer.getPosition(), 1);

        if (starter != null && starter.getPlayer() != null && !starter.getPlayer().getId().equals(injuredPlayer.getId())) {
            return starter;
        }

        return depthChartRepository.findByTeamAndSeasonAndWeekAndPositionAndDepthRank(
                team, game.getSeason(), game.getWeek(), injuredPlayer.getPosition(), 2);
    }

    //calculates total yards for a player across all categories
    private int totalYards(PlayerStats ps) {
        return nullToZero(ps.getPassingYards())
             + nullToZero(ps.getRushingYards())
             + nullToZero(ps.getReceivingYards());
    }


    private int nullToZero(Integer value) {
        return value != null ? value : 0;
    }
}
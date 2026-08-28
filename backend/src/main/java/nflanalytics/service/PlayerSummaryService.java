package nflanalytics.service;

import java.util.List;
import java.util.OptionalDouble;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.Contract;
import nflanalytics.model.Injury;
import nflanalytics.model.Player;
import nflanalytics.model.PlayerStats;
import nflanalytics.model.SnapCount;
import nflanalytics.repository.ContractRepository;
import nflanalytics.repository.InjuryRepository;
import nflanalytics.repository.PlayerStatsRepository;
import nflanalytics.repository.SnapCountRepository;

@Service
@RequiredArgsConstructor
public class PlayerSummaryService {

    private final PlayerStatsRepository playerStatsRepository;
    private final InjuryRepository injuryRepository;
    private final SnapCountRepository snapCountRepository;
    private final ContractRepository contractRepository;

    // builds a single coherent text document for a player's full season
    // combining stats, injuries, snap usage, and contract into one chunk
    // this makes cross-table questions answerable in a single retrieval step
    // e.g. "did player X play less because of injury or loss of starting role?"
    public String buildSeasonSummary(Player player, Integer season) {
        StringBuilder sb = new StringBuilder();

        // player identity header
        sb.append("PLAYER SEASON PROFILE\n");
        sb.append("Player: ").append(player.getFullName()).append("\n");
        sb.append("Position: ").append(player.getPosition()).append("\n");
        sb.append("Season: ").append(season).append("\n");
        if (player.getTeam() != null) {
            sb.append("Team: ").append(player.getTeam().getName())
              .append(" (").append(player.getTeam().getAbbreviation()).append(")\n");
        }
        sb.append("\n");

        // aggregate season stats across all games
        List<PlayerStats> statsList = playerStatsRepository.findByPlayer_IdAndGame_Season(player.getId(), season);
        if (!statsList.isEmpty()) {
            sb.append("SEASON STATISTICS (").append(statsList.size()).append(" games):\n");
            appendAggregateStats(sb, statsList);
            sb.append("\n");
        }

        // injury history — what was reported and when
        List<Injury> injuries = injuryRepository.findByPlayer_IdAndSeasonOrderByWeek(player.getId(), season);
        if (!injuries.isEmpty()) {
            sb.append("INJURY REPORT (").append(injuries.size()).append(" entries):\n");
            for (Injury i : injuries) {
                sb.append("- Week ").append(i.getWeek()).append(": ")
                  .append(i.getReportStatus() != null ? i.getReportStatus() : "Listed");
                if (i.getReportPrimaryInjury() != null)
                    sb.append(" — ").append(i.getReportPrimaryInjury());
                if (i.getPracticeStatus() != null)
                    sb.append(" [practice: ").append(i.getPracticeStatus()).append("]");
                sb.append("\n");
            }

            // count confirmed missed games: "Out" with no stats in that game
            long gamesOut = injuries.stream()
                    .filter(i -> "Out".equalsIgnoreCase(i.getReportStatus()))
                    .filter(i -> i.getGame() != null)
                    .filter(i -> statsList.stream().noneMatch(ps -> ps.getGame().getId().equals(i.getGame().getId())))
                    .count();
            if (gamesOut > 0) {
                sb.append("Confirmed games missed due to injury: ").append(gamesOut).append("\n");
            }
            sb.append("\n");
        }

        // snap count usage — shows whether reduced stats reflect reduced role
        List<SnapCount> snaps = snapCountRepository.findByPlayer_IdAndSeasonOrderByWeek(player.getId(), season);
        if (!snaps.isEmpty()) {
            OptionalDouble avgOffensePct = snaps.stream()
                    .filter(sc -> sc.getOffensePct() != null)
                    .mapToDouble(SnapCount::getOffensePct)
                    .average();
            OptionalDouble avgDefensePct = snaps.stream()
                    .filter(sc -> sc.getDefensePct() != null)
                    .mapToDouble(SnapCount::getDefensePct)
                    .average();

            sb.append("SNAP COUNT USAGE (").append(snaps.size()).append(" weeks):\n");
            avgOffensePct.ifPresent(v -> sb.append("- Avg offensive snap %: ")
                    .append(String.format("%.1f", v)).append("%\n"));
            avgDefensePct.ifPresent(v -> sb.append("- Avg defensive snap %: ")
                    .append(String.format("%.1f", v)).append("%\n"));

            // flag significant week-over-week drops in usage (> 30 pp)
            // same threshold used in PlayerUsageService
            boolean hasDrops = false;
            for (int i = 1; i < snaps.size(); i++) {
                SnapCount prev = snaps.get(i - 1);
                SnapCount curr = snaps.get(i);
                if (prev.getOffensePct() != null && curr.getOffensePct() != null) {
                    double drop = prev.getOffensePct() - curr.getOffensePct();
                    if (drop > 30) {
                        if (!hasDrops) { sb.append("- Notable usage drops:\n"); hasDrops = true; }
                        sb.append("  Week ").append(curr.getWeek()).append(": dropped ")
                          .append(String.format("%.0f", drop)).append(" pp (")
                          .append(String.format("%.0f", prev.getOffensePct())).append("% → ")
                          .append(String.format("%.0f", curr.getOffensePct())).append("%)\n");
                    }
                }
            }
            sb.append("\n");
        }

        // active contract context — salary and years give context to performance expectations
        List<Contract> activeContracts = contractRepository.findByPlayer_IdAndIsActiveTrue(player.getId());
        if (!activeContracts.isEmpty()) {
            Contract c = activeContracts.get(0);
            sb.append("CONTRACT:\n");
            if (c.getTotalValue() != null)
                sb.append("- Total value: $").append(String.format("%.1fM", c.getTotalValue() / 1_000_000)).append("\n");
            if (c.getApy() != null)
                sb.append("- APY: $").append(String.format("%.1fM", c.getApy() / 1_000_000)).append("\n");
            if (c.getYears() != null)
                sb.append("- Length: ").append(c.getYears()).append(" years (signed ").append(c.getYearSigned()).append(")\n");
            if (c.getApyCapPct() != null)
                sb.append("- Cap %: ").append(String.format("%.2f", c.getApyCapPct())).append("%\n");
        }

        return sb.toString().trim();
    }

    // sums all per-game stats into season totals for the summary document
    private void appendAggregateStats(StringBuilder sb, List<PlayerStats> stats) {
        int passAtt = 0, passComp = 0, passYds = 0, passTds = 0, ints = 0;
        int rushAtt = 0, rushYds = 0, rushTds = 0;
        int targets = 0, rec = 0, recYds = 0, recTds = 0;
        int tackles = 0, sacks = 0, ff = 0;

        for (PlayerStats ps : stats) {
            passAtt  += n(ps.getPassingAttempts());
            passComp += n(ps.getPassingCompletions());
            passYds  += n(ps.getPassingYards());
            passTds  += n(ps.getPassingTouchdowns());
            ints     += n(ps.getInterceptions());
            rushAtt  += n(ps.getRushingAttempts());
            rushYds  += n(ps.getRushingYards());
            rushTds  += n(ps.getRushingTouchdowns());
            targets  += n(ps.getTargets());
            rec      += n(ps.getReceptions());
            recYds   += n(ps.getReceivingYards());
            recTds   += n(ps.getReceivingTouchdowns());
            tackles  += n(ps.getTackles());
            sacks    += n(ps.getSacks());
            ff       += n(ps.getForcedFumbles());
        }

        if (passAtt > 0)
            sb.append("- Passing: ").append(passComp).append("/").append(passAtt)
              .append(", ").append(passYds).append(" yds, ").append(passTds)
              .append(" TDs, ").append(ints).append(" INTs\n");
        if (rushAtt > 0)
            sb.append("- Rushing: ").append(rushAtt).append(" att, ").append(rushYds)
              .append(" yds, ").append(rushTds).append(" TDs\n");
        if (targets > 0)
            sb.append("- Receiving: ").append(rec).append("/").append(targets)
              .append(" targets, ").append(recYds).append(" yds, ").append(recTds).append(" TDs\n");
        if (tackles > 0 || sacks > 0 || ff > 0)
            sb.append("- Defense: ").append(tackles).append(" tackles, ").append(sacks)
              .append(" sacks, ").append(ff).append(" forced fumbles\n");
    }

    private int n(Integer v) {
        return v != null ? v : 0;
    }
}

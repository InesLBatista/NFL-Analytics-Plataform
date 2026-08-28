package nflanalytics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DocumentChunk;
import nflanalytics.model.GameReport;
import nflanalytics.model.Player;
import nflanalytics.model.Team;
import nflanalytics.repository.DocumentChunkRepository;
import nflanalytics.repository.GameReportRepository;
import nflanalytics.repository.PlayerStatsRepository;
import nflanalytics.repository.TeamRepository;

@Service
@RequiredArgsConstructor
public class RagIngestionService {
    private final GameReportRepository gameReportRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final VoyageEmbeddingClient embeddingClient;
    private final JdbcTemplate jdbcTemplate;
    private final PlayerSummaryService playerSummaryService;
    private final PlayerStatsRepository playerStatsRepository;
    private final TeamSummaryService teamSummaryService;
    private final TeamRepository teamRepository;

    //index all GameReport without a DocumentChunk associated, no calling it again processes only the new ones
    public int indexAllGameReports() throws Exception {
        List<GameReport> reports = gameReportRepository.findAll();
        int indexed = 0;

        for (GameReport report : reports) {
            if (documentChunkRepository.existsBySourceTypeAndSourceId("game_report", report.getGame().getId())) {
                continue;
            }

            DocumentChunk chunk = new DocumentChunk();
            chunk.setContent(report.getContent());
            chunk.setSourceType("game_report");
            chunk.setSourceId(report.getGame().getId());
            chunk.setSeason(report.getGame().getSeason());
            chunk.setWeek(report.getGame().getWeek());
            chunk.setCreatedAt(LocalDateTime.now());


            //id generation
            chunk = documentChunkRepository.save(chunk); 

            float[] embedding = embeddingClient.embed(report.getContent(), "document");
            String vectorLiteral = embeddingClient.toVectorLiteral(embedding);

            jdbcTemplate.update("UPDATE document_chunks SET embedding = ?::vector WHERE id = ?",
                    vectorLiteral, chunk.getId());

            indexed++;



            Thread.sleep(200);
        }

        return indexed;
    }


    public int indexPlayerSeasonSummaries(Integer season) throws Exception {
        List<Player> players = playerStatsRepository.findDistinctPlayersBySeason(season);
        int indexed = 0;

        for (Player player : players) {
            if (documentChunkRepository.existsBySourceTypeAndSourceIdAndSeason("player_season_summary", player.getId(), season)) {
                continue;
            }

            String summaryText = playerSummaryService.buildSeasonSummary(player, season);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setContent(summaryText);
            chunk.setSourceType("player_season_summary");
            chunk.setSourceId(player.getId());
            chunk.setSeason(season);
            chunk.setCreatedAt(LocalDateTime.now());

            chunk = documentChunkRepository.save(chunk);

            float[] embedding = embeddingClient.embed(summaryText, "document");
            String vectorLiteral = embeddingClient.toVectorLiteral(embedding);

            jdbcTemplate.update("UPDATE document_chunks SET embedding = ?::vector WHERE id = ?", vectorLiteral, chunk.getId());

            indexed++;
            Thread.sleep(200); 
        }
        return indexed;
    }

    public int indexTeamSeasonSummaries(Integer season) throws Exception {
        List<Team> teams = teamRepository.findAll();
        int indexed = 0;

        for (Team team : teams) {
            if (documentChunkRepository.existsBySourceTypeAndSourceIdAndSeason("team_season_summary", team.getId(), season)) {
                continue;
            }

            String summaryText = teamSummaryService.buildSeasonSummary(team, season);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setContent(summaryText);
            chunk.setSourceType("team_season_summary");
            chunk.setSourceId(team.getId());
            chunk.setSeason(season);
            chunk.setCreatedAt(LocalDateTime.now());

            chunk = documentChunkRepository.save(chunk);

            float[] embedding = embeddingClient.embed(summaryText, "document");
            String vectorLiteral = embeddingClient.toVectorLiteral(embedding);

            jdbcTemplate.update("UPDATE document_chunks SET embedding = ?::vector WHERE id = ?",
                    vectorLiteral, chunk.getId());

            indexed++;
            Thread.sleep(200);
        }
        return indexed;
    }
}

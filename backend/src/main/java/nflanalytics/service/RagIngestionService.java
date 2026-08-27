package nflanalytics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DocumentChunk;
import nflanalytics.model.GameReport;
import nflanalytics.repository.DocumentChunkRepository;
import nflanalytics.repository.GameReportRepository;

@Service
@RequiredArgsConstructor
public class RagIngestionService {
    private final GameReportRepository gameReportRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final VoyageEmbeddingClient embeddingClient;
    private final JdbcTemplate jdbcTemplate;

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
}

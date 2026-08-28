package nflanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DocumentChunk;
import nflanalytics.repository.DocumentChunkRepository;

@Service
@RequiredArgsConstructor
public class DocumentChunkService {

    private final DocumentChunkRepository documentChunkRepository;
    private final RagIngestionService ragIngestionService;

    //returns all indexed document chunks, useful for admin inspection
    public List<DocumentChunk> getAllChunks() {
        return documentChunkRepository.findAll();
    }

    public DocumentChunk getChunkById(Long id) {
        return documentChunkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DocumentChunk not found with id: " + id));
    }

    //removes all chunks for a given source and re-indexes from scratch
    //useful when a game report is regenerated and the old embedding is stale
    public void reindex(String sourceType, Long sourceId) throws Exception {
        documentChunkRepository.deleteBySourceTypeAndSourceId(sourceType, sourceId);
        ragIngestionService.indexAllGameReports();
    }

    //runs the full indexing pipeline for any game report not yet embedded
    public int indexPending() throws Exception {
        return ragIngestionService.indexAllGameReports();
    }

    //indexes a coherent player-season profile for every player that appeared in a given season
    //combines stats, injuries, snap usage, and contract into a single document per player
    public int indexPlayerSeasonSummaries(Integer season) throws Exception {
        return ragIngestionService.indexPlayerSeasonSummaries(season);
    }
}

package nflanalytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nflanalytics.model.DocumentChunk;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    boolean existsBySourceTypeAndSourceId(String sourceType, Long sourceId);
    void deleteBySourceTypeAndSourceId(String sourceType, Long sourceId);
}

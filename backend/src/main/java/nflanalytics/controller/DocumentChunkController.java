package nflanalytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.model.DocumentChunk;
import nflanalytics.service.DocumentChunkService;

@RestController
@RequestMapping("/api/admin/rag")
@RequiredArgsConstructor
public class DocumentChunkController {

    private final DocumentChunkService documentChunkService;

    @GetMapping("/chunks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentChunk>> getAllChunks() {
        return ResponseEntity.ok(documentChunkService.getAllChunks());
    }

    @GetMapping("/chunks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentChunk> getChunkById(@PathVariable Long id) {
        return ResponseEntity.ok(documentChunkService.getChunkById(id));
    }

    @PostMapping("/index")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> indexPending() {
        try {
            int indexed = documentChunkService.indexPending();
            return ResponseEntity.ok("Indexed " + indexed + " new document chunks");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Indexing failed: " + e.getMessage());
        }
    }

    //removes the existing embedding for a source and re-indexes it
    //use when a game report has been regenerated and its embedding is stale
    @DeleteMapping("/chunks/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> reindex(
            @RequestParam String sourceType,
            @RequestParam Long sourceId) {
        try {
            documentChunkService.reindex(sourceType, sourceId);
            return ResponseEntity.ok("Re-indexed source: " + sourceType + " / " + sourceId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Re-index failed: " + e.getMessage());
        }
    }
}

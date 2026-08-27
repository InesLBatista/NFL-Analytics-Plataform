package nflanalytics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nflanalytics.dto.AssistantRequest;
import nflanalytics.dto.AssistantResponse;
import nflanalytics.service.RagQueryService;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class AssistantController {

    private final RagQueryService ragQueryService;

    //receives a natural language question, runs the RAG pipeline, and returns the answer
    //open to all authenticated and unauthenticated users — access restricted at the security layer if needed
    @PostMapping("/ask")
    public ResponseEntity<AssistantResponse> ask(@RequestBody AssistantRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String answer = ragQueryService.ask(request.question());
            return ResponseEntity.ok(new AssistantResponse(request.question(), answer));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new AssistantResponse(request.question(), "Failed to process question: " + e.getMessage()));
        }
    }
}

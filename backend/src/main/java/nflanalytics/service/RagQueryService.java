package nflanalytics.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RagQueryService {

    private final VoyageEmbeddingClient embeddingClient;
    private final AnthropicClient anthropicClient;
    private final JdbcTemplate jdbcTemplate;

    public String ask(String question) throws Exception {
        //embed the question into a vector using the same model used during ingestion
        float[] questionEmbedding = embeddingClient.embed(question, "query");
        String vectorLiteral = embeddingClient.toVectorLiteral(questionEmbedding);

        //"<->" is the pgvector cosine distance operator — lower value means higher similarity
        //retrieve the 5 most relevant chunks for the given question
        List<String> relevantChunks = jdbcTemplate.query(
                "SELECT content FROM document_chunks ORDER BY embedding <-> ?::vector LIMIT 5",
                (rs, rowNum) -> rs.getString("content"),
                vectorLiteral
        );

        if (relevantChunks.isEmpty()) {
            return "There is not enough indexed data available to answer that question yet.";
        }

        //join retrieved chunks into a single context block, separated by delimiters
        String context = relevantChunks.stream()
                .map(c -> "---\n" + c)
                .collect(Collectors.joining("\n\n"));

        //build the prompt with the retrieved context and the user's question
        //the model is instructed to stay grounded in the provided data and not fabricate statistics
        String prompt = buildPrompt(question, context);

        return anthropicClient.generateText(prompt);
    }

    private String buildPrompt(String question, String context) {
        return "You are an expert NFL analyst and sports journalist assistant with deep knowledge " +
               "of professional football statistics, team strategy, player performance, and league history. " +
               "Answer the user's question using only the context provided below. " +
               "Use precise football terminology and a professional analytical tone. " +
               "If the context does not contain enough information to answer accurately, " +
               "state that clearly rather than speculating or inventing statistics.\n\n" +
               "CONTEXT:\n" + context + "\n\n" +
               "QUESTION: " + question + "\n\nANSWER:";
    }
}

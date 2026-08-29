package nflanalytics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RagQueryService {
    private final QueryAnalysisService queryAnalysisService;
    private final VoyageEmbeddingClient embeddingClient;
    private final AnthropicClient anthropicClient;
    private final JdbcTemplate jdbcTemplate;

    public String ask(String question) throws Exception {
        QueryAnalysisService.QueryFilters filters = queryAnalysisService.analyze(question);

        //embed the question into a vector using the same model used during ingestion
        float[] questionEmbedding = embeddingClient.embed(question, "query");
        String vectorLiteral = embeddingClient.toVectorLiteral(questionEmbedding);

        
        //will build SQL query dynamically according to returned filters
        StringBuilder sql = new StringBuilder("SELECT content FROM document_chunks WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if(filters.season() != null) {
            sql.append(" AND season = ?");
            params.add(filters.season());
        }
        if (filters.sourceType() != null) {
            sql.append(" AND source_type = ?");
            params.add(filters.season());
        }

        sql.append(" ORDER BY embedding <-> ?::vector LIMIT 6");
        params.add(vectorLiteral);

        List<String> relevantChunks = jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> rs.getString("content"),
                params.toArray()
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
        String prompt = "You are an expert NFL analyst and sports journalist assistant with deep knowledge " +
               "of professional football statistics, team strategy, player performance, and league history. " +
               "Answer the user's question using only the context provided below. " +
               "Use precise football terminology and a professional analytical tone. " +
               "If the context does not contain enough information to answer accurately, " +
               "state that clearly rather than speculating or inventing statistics.\n\n" +
               "CONTEXT:\n" + context + "\n\n" +
               "QUESTION: " + question + "\n\nANSWER:";

        return anthropicClient.generateText(prompt);
    }
}

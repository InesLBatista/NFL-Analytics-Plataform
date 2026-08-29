package nflanalytics.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

//extraction of structured filters from a question before vectorial search
@Service
@RequiredArgsConstructor
public class QueryAnalysisService {
    private final AnthropicClient anthropicClient;
    private final ObjectMapper ObjectMapper = new ObjectMapper();

    public record QueryFilters(Integer season, String sourceType) {}

    public QueryFilters analyze(String question) {
        String prompt = "Analyze the user's question about NFL and extract two fields, " +
                "responding ONLY with a JSON object, without additional text or markdown:\n\n" +
                "- \"season\": the year of the season mentioned (e.g., 2024), or null if not mentioned\n" +
                "- \"sourceType\": one of these exact values, or null if not clear:\n" +
                "  \"game_report\" (question about a specific game)\n" +
                "  \"player_season_summary\" (question about a player)\n" +
                "  \"team_season_summary\" (question about a team)\n\n" +
                "Question: \"" + question + "\"\n\n" +
                "Respond only with the JSON, for example: {\"season\": 2024, \"sourceType\": \"team_season_summary\"}";

        try {
            String response = anthropicClient.generateText(prompt);
            String cleanJson = response.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode node = ObjectMapper.readTree(cleanJson);

            Integer season = node.path("season").isNull() ? null : node.path("season").asInt();
            String sourceType = node.path("sourceType").isNull() ? null : node.path("sourceType").asText();

            return new QueryFilters(season, sourceType);
        } catch (Exception e) {
            //in case of error, degrading into no filters search
            System.out.println("Error analyzing the question, proceding without filters: " + e.getMessage());
            return new QueryFilters(null, null);
        }
    }
}

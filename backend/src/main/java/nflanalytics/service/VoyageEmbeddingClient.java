package nflanalytics.service;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

//client to generate embeddings from text
@Service
public class VoyageEmbeddingClient {
    @Value("${voyage.api-key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //inputType: "document" to index text, "query" for the search
    public float[] embed(String text, String inputType) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("VOYAGE_API_KEY not defined.");
        }

        Map<String, Object> requestBody = Map.of(
                "input", List.of(text),
                "model", "voyage-3-lite",
                "input_type", inputType
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.voyageai.com/v1/embeddings"))
                .header("Authorization", "Bearer " + apiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error no Voyage's API: " + response.statusCode() + " — " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode embeddingNode = root.path("data").get(0).path("embedding");

        float[] embedding = new float[embeddingNode.size()];
        for (int i = 0; i < embeddingNode.size(); i++) {
            embedding[i] = (float) embeddingNode.get(i).asDouble();
        }

        return embedding;
    }


    //to convert the vector into the text format expected by pgvector 
    public String toVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    } 
}

package nflanalytics.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

//correção de import de propriedades face ao Value de lombok que não as injeta
import org.springframework.beans.factory.annotation.Value;

//only jave.net.http
@Service
public class AnthropicClient {
    @Value("${anthropic.api-key}")
    private String apiKey;


    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //sending the prompt and getting its response
    public String generateText(String prompt) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("No ANTHROPIC_API_KEY defined.");
        }

        Map<String, Object> requestBody = Map.of(
            "model", "claude-sonnet-5",
            "max_tokens", 1024,
            "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        String json = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error on Anthropic's API: " + response.statusCode() + " - " + response.body());
        }


        //response as blocks of content transformed into blocks of text
        JsonNode root = objectMapper.readTree(response.body());
        StringBuilder result = new StringBuilder();
        
        JsonNode contentArray = root.get("content");
        if (contentArray != null && contentArray.isArray()) {
            for (JsonNode block : contentArray) {
                JsonNode typeNode = block.get("type");
                JsonNode textNode = block.get("text");
                
                if (typeNode != null && "text".equals(typeNode.asString()) && textNode != null) {
                    result.append(textNode.asString());
                }
            }
        }

        return result.toString();
    }
}


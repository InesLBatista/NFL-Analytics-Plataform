package nflanalytics.dto;

//response body returned by the RAG assistant endpoint
public record AssistantResponse(String question, String answer) {}

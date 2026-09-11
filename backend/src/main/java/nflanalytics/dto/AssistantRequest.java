package nflanalytics.dto;

// request body for the RAG assistant endpoint
// sessionId is generated on the frontend and passed with every request from the same session
// it is used only for grouping log entries — it is never persisted
public record AssistantRequest(String question, String sessionId) {}

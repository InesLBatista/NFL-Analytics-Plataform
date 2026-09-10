import { useState, useRef, useEffect } from "react";
import { apiClient, ApiError } from "../apiClient";
import type { AssistantResponse, ChatMessage } from "../types";
import { colors, spacing } from "../theme";

const SUGGESTED_QUESTIONS = [
    "How was the Kansas City Chiefs' 2024 season?",
    "Which players saw a sharp drop in playing time due to injury?",
    "Summarize the most recent game between the 49ers and the Cowboys.",
];

function AssistantPage() {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [input, setInput] = useState("");
    const [sending, setSending] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const scrollRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        scrollRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    function sendMessage(question: string) {
        const trimmed = question.trim();
        if (trimmed === "" || sending) return;

        const userMessage: ChatMessage = { id: crypto.randomUUID(), role: "user", content: trimmed };
        setMessages((prev) => [...prev, userMessage]);
        setInput("");
        setSending(true);
        setError(null);

        apiClient
            .post<AssistantResponse>("/api/assistant/ask", { question: trimmed })
            .then((response) => {
                const assistantMessage: ChatMessage = {
                    id: crypto.randomUUID(),
                    role: "assistant",
                    content: response.answer,
                };
                setMessages((prev) => [...prev, assistantMessage]);
                setSending(false);
            })
            .catch((err: ApiError) => {
                setError(err.message);
                setSending(false);
            });
    }

    function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        sendMessage(input);
    }

    return (
        <div style={{ display: "flex", flexDirection: "column", height: "calc(100vh - 200px)" }}>
            <h1 style={{ fontSize: 22, color: colors.textPrimary, marginBottom: spacing.sm }}>
                NFL Assistant
            </h1>
            <p style={{ fontSize: 13, color: colors.textSecondary, marginBottom: spacing.md }}>
                Faz perguntas sobre jogos, jogadores e equipas com base nos dados da plataforma.
            </p>

            {/* Área de mensagens, com scroll independente do resto da página */}
            <div
                style={{
                    flex: 1,
                    overflowY: "auto",
                    border: `1px solid ${colors.border}`,
                    borderRadius: 8,
                    padding: spacing.md,
                    backgroundColor: colors.surface,
                    marginBottom: spacing.md,
                }}
            >
                {messages.length === 0 && (
                    <div>
                        <p style={{ fontSize: 13, color: colors.textSecondary, marginBottom: spacing.sm }}>
                            Experimenta perguntar:
                        </p>
                        {SUGGESTED_QUESTIONS.map((q) => (
                            <button
                                key={q}
                                onClick={() => sendMessage(q)}
                                style={{
                                    display: "block",
                                    textAlign: "left",
                                    width: "100%",
                                    border: `1px solid ${colors.border}`,
                                    borderRadius: 6,
                                    padding: spacing.sm,
                                    marginBottom: spacing.sm,
                                    backgroundColor: colors.background,
                                    color: colors.textPrimary,
                                    fontSize: 13,
                                    cursor: "pointer",
                                }}
                            >
                                {q}
                            </button>
                        ))}
                    </div>
                )}

                {messages.map((message) => (
                    <ChatBubble key={message.id} message={message} />
                ))}

                {sending && (
                    <div style={{ fontSize: 13, color: colors.textSecondary, fontStyle: "italic" }}>
                        A pensar...
                    </div>
                )}

                {error && (
                    <div style={{ fontSize: 13, color: colors.danger, marginTop: spacing.sm }}>
                        Error: {error}
                    </div>
                )}

                <div ref={scrollRef} />
            </div>

            {/* Campo de input */}
            <form onSubmit={handleSubmit} style={{ display: "flex", gap: spacing.sm }}>
                <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder="Ask something about the NFL."
                    disabled={sending}
                    style={{
                        flex: 1,
                        padding: `${spacing.sm}px ${spacing.md}px`,
                        border: `1px solid ${colors.border}`,
                        borderRadius: 6,
                        fontSize: 14,
                        outline: "none",
                    }}
                />
                <button
                    type="submit"
                    disabled={sending || input.trim() === ""}
                    style={{
                        border: "none",
                        borderRadius: 6,
                        padding: `${spacing.sm}px ${spacing.lg}px`,
                        backgroundColor: sending ? colors.border : colors.accent,
                        color: "#FFFFFF",
                        fontSize: 14,
                        fontWeight: 600,
                        cursor: sending ? "default" : "pointer",
                    }}
                >
                    Send
                </button>
            </form>
        </div>
    );
}

interface ChatBubbleProps {
    message: ChatMessage;
}

function ChatBubble({ message }: ChatBubbleProps) {
    const isUser = message.role === "user";

    return (
        <div
            style={{
                display: "flex",
                justifyContent: isUser ? "flex-end" : "flex-start",
                marginBottom: spacing.sm,
            }}
        >
            <div
                style={{
                    maxWidth: "80%",
                    padding: `${spacing.sm}px ${spacing.md}px`,
                    borderRadius: 12,
                    backgroundColor: isUser ? colors.accent : colors.background,
                    color: isUser ? "#FFFFFF" : colors.textPrimary,
                    border: isUser ? "none" : `1px solid ${colors.border}`,
                    fontSize: 14,
                    lineHeight: 1.5,
                    whiteSpace: "pre-line",
                }}
            >
                {message.content}
            </div>
        </div>
    );
}

export default AssistantPage;

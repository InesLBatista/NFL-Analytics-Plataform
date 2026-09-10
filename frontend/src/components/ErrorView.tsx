import { colors, spacing } from "../theme";

interface ErrorViewProps {
    message: string;
    onRetry?: () => void;
}

function ErrorView({ message, onRetry }: ErrorViewProps) {
    return (
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", padding: 48, gap: spacing.sm }}>
            <span style={{ color: colors.danger }}>Error: {message}</span>
            {onRetry && (
                <button
                    onClick={onRetry}
                    style={{
                        border: `1px solid ${colors.border}`,
                        borderRadius: 6,
                        padding: `${spacing.sm}px ${spacing.md}px`,
                        background: "none",
                        cursor: "pointer",
                        color: colors.textPrimary,
                    }}
                >
                    Try again
                </button>
            )}
        </div>
    );
}

export default ErrorView;
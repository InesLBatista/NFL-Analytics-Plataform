import { colors, spacing } from "../theme";

interface ErrorViewProps {
    message: string;
    onRetry?: () => void;
}

function ErrorView({ message, onRetry }: ErrorViewProps) {
    return (
        <div style={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", padding: `0 ${spacing.md}px` }}>
            <p style={{ color: colors.danger, textAlign: "center", marginBottom: spacing.sm }}>
                Error: {message}
            </p>
            {onRetry && (
                <button
                    onClick={onRetry}
                    style={{
                        border: `1px solid ${colors.border}`,
                        padding: `${spacing.sm}px ${spacing.md}px`,
                        borderRadius: 6,
                        background: "transparent",
                        color: colors.textPrimary,
                        cursor: "pointer",
                        fontSize: 14,
                    }}
                >
                    Try again
                </button>
            )}
        </div>
    );
}

export default ErrorView;

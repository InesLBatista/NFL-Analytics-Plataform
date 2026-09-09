import { colors } from "../theme";

interface LoadingViewProps {
    message?: string;
}

function LoadingView({ message = "Loading..." }: LoadingViewProps) {
    return (
        <div style={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", minHeight: 200 }}>
            {/* spinner built with a CSS animation defined in index.css */}
            <div className="spinner" style={{ width: 36, height: 36, borderRadius: "50%", border: `3px solid ${colors.border}`, borderTopColor: colors.accent }} />
            <p style={{ marginTop: 8, color: colors.textSecondary }}>{message}</p>
        </div>
    );
}

export default LoadingView;

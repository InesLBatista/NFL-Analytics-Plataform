import { colors } from "../theme";

interface LoadingViewProps {
    message?: string;
}

function LoadingView({ message = "Loading..." }: LoadingViewProps) {
    return (
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", padding: 48 }}>
            <div style={{ color: colors.textSecondary, fontSize: 14 }}>{message}</div>
        </div>
    );
}

export default LoadingView;
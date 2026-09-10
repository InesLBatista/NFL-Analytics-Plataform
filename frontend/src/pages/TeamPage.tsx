import { useState, useEffect } from "react";
import { apiClient, ApiError } from "../apiClient";
import type { Team } from "../types";
import { colors, spacing } from "../theme";
import LoadingView from "../components/LoadingView";
import ErrorView from "../components/ErrorView";

function TeamsPage() {
    const [teams, setTeams] = useState<Team[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    function fetchTeams() {
        setLoading(true);
        setError(null);

        apiClient
            .get<Team[]>("/api/teams")
            .then((data) => {
                setTeams(data);
                setLoading(false);
            })
            .catch((err: ApiError) => {
                setError(err.message);
                setLoading(false);
            });
    }

    useEffect(() => {
        fetchTeams();
    }, []);

    if (loading) return <LoadingView message="Loading teams..." />;
    if (error) return <ErrorView message={error} onRetry={fetchTeams} />;

    return (
        <div>
            <h1 style={{ fontSize: 22, color: colors.textPrimary, marginBottom: spacing.md }}>
                NFL Teams
            </h1>

            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", gap: spacing.md }}>
                {teams.map((team) => (
                    <div
                        key={team.id}
                        style={{
                            border: `1px solid ${colors.border}`,
                            borderRadius: 8,
                            padding: spacing.md,
                            backgroundColor: colors.surface,
                        }}
                    >
                        <div style={{ fontWeight: 600, color: colors.textPrimary }}>{team.name}</div>
                        <div style={{ fontSize: 13, color: colors.textSecondary, marginTop: 4 }}>
                            {team.abbreviation} — {team.conference} {team.division}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default TeamsPage;
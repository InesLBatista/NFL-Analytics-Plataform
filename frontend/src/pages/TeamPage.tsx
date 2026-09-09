import { useState, useEffect } from "react";
import ScreenContainer from "../components/ScreenContainer";
import LoadingView from "../components/LoadingView";
import ErrorView from "../components/ErrorView";
import { apiClient, ApiError } from "../apiClient";
import { Team } from "../types";
import { colors, spacing } from "../theme";

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
        <ScreenContainer scrollable={false}>
            <h1 style={{ fontSize: 22, fontWeight: 700, color: colors.textPrimary, marginBottom: spacing.md }}>
                NFL Teams
            </h1>
            <div>
                {teams.map((team) => (
                    <div
                        key={team.id}
                        style={{ paddingTop: spacing.sm, paddingBottom: spacing.sm, borderBottom: `1px solid ${colors.border}` }}
                    >
                        <span style={{ color: colors.textPrimary }}>
                            {team.name} ({team.abbreviation}) — {team.conference} {team.division}
                        </span>
                    </div>
                ))}
            </div>
        </ScreenContainer>
    );
}

export default TeamsPage;

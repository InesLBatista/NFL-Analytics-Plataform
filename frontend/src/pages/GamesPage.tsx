import { useState, useEffect } from "react";
import ScreenContainer from "../components/ScreenContainer";
import LoadingView from "../components/LoadingView";
import ErrorView from "../components/ErrorView";
import { apiClient, ApiError } from "../apiClient";
import { Game } from "../types";
import { colors, spacing } from "../theme";

function GamesPage() {
    const [season] = useState(2024);
    const [week, setWeek] = useState(1);
    const [games, setGames] = useState<Game[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    function fetchGames() {
        setLoading(true);
        setError(null);

        apiClient
            .get<Game[]>(`/api/games/season/${season}/week/${week}`)
            .then((data) => {
                setGames(data);
                setLoading(false);
            })
            .catch((err: ApiError) => {
                setError(err.message);
                setLoading(false);
            });
    }

    useEffect(() => {
        fetchGames();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [season, week]);

    function goToPreviousWeek() {
        if (week > 1) setWeek(week - 1);
    }

    function goToNextWeek() {
        if (week < 18) setWeek(week + 1);
    }

    if (loading) return <LoadingView message="Loading games..." />;
    if (error) return <ErrorView message={error} onRetry={fetchGames} />;

    return (
        <ScreenContainer>
            <h1 style={{ fontSize: 22, fontWeight: 700, color: colors.textPrimary, margin: 0 }}>
                NFL Games
            </h1>
            <p style={{ fontSize: 14, color: colors.textSecondary, marginBottom: spacing.md, marginTop: spacing.xs }}>
                Season {season} — Week {week}
            </p>

            {/* week navigation */}
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: spacing.lg }}>
                <button
                    onClick={goToPreviousWeek}
                    disabled={week <= 1}
                    style={{ border: `1px solid ${colors.border}`, padding: `${spacing.sm}px ${spacing.md}px`, borderRadius: 6, background: "transparent", color: colors.textPrimary, cursor: week <= 1 ? "not-allowed" : "pointer", opacity: week <= 1 ? 0.4 : 1 }}
                >
                    ← Previous
                </button>
                <button
                    onClick={goToNextWeek}
                    disabled={week >= 18}
                    style={{ border: `1px solid ${colors.border}`, padding: `${spacing.sm}px ${spacing.md}px`, borderRadius: 6, background: "transparent", color: colors.textPrimary, cursor: week >= 18 ? "not-allowed" : "pointer", opacity: week >= 18 ? 0.4 : 1 }}
                >
                    Next →
                </button>
            </div>

            {/* game list */}
            {games.length === 0 ? (
                <p style={{ color: colors.textSecondary }}>No games found for this week.</p>
            ) : (
                games.map((game) => (
                    <div
                        key={game.id}
                        style={{ display: "flex", justifyContent: "space-between", paddingTop: spacing.sm, paddingBottom: spacing.sm, borderBottom: `1px solid ${colors.border}` }}
                    >
                        <span style={{ color: colors.textPrimary }}>
                            {game.awayTeam.abbreviation} @ {game.homeTeam.abbreviation}
                        </span>
                        <span style={{ color: colors.textSecondary }}>
                            {game.status === "FINAL"
                                ? `${game.awayScore} - ${game.homeScore}`
                                : game.status}
                        </span>
                    </div>
                ))
            )}
        </ScreenContainer>
    );
}

export default GamesPage;

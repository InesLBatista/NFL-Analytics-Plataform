import { useState, useEffect } from "react";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { apiClient, ApiError } from "../apiClient";
import type { Game } from "../types";
import { colors, spacing } from "../theme";
import LoadingView from "../components/LoadingView";
import ErrorView from "../components/ErrorView";
import { Link } from "react-router-dom";

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
    }, [season, week]);

    if (loading) return <LoadingView message="Loading games..." />;
    if (error) return <ErrorView message={error} onRetry={fetchGames} />;

    const finishedGames = games.filter((g) => g.status === "FINAL");

    const chartData = finishedGames.map((g) => ({
        matchup: g.homeTeam.abbreviation,
        margin: (g.homeScore ?? 0) - (g.awayScore ?? 0),
    }));

    return (
        <div>
            <h1 style={{ fontSize: 22, color: colors.textPrimary, marginBottom: 4 }}>NFL Games</h1>
            <p style={{ fontSize: 14, color: colors.textSecondary, marginBottom: spacing.md }}>
                Season {season} — Week {week}
            </p>

            <div style={{ display: "flex", gap: spacing.sm, marginBottom: spacing.lg }}>
                <button
                    onClick={() => week > 1 && setWeek(week - 1)}
                    style={buttonStyle}
                >
                    ← Previous
                </button>
                <button
                    onClick={() => week < 18 && setWeek(week + 1)}
                    style={buttonStyle}
                >
                    Next →
                </button>
            </div>

            {chartData.length > 0 && (
                <div style={{ marginBottom: spacing.xl }}>
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        Margem de vitória (equipa da casa)
                    </h3>
                    <ResponsiveContainer width="100%" height={260}>
                        <BarChart data={chartData}>
                            <CartesianGrid stroke={colors.border} vertical={false} />
                            <XAxis dataKey="matchup" tick={{ fontSize: 12, fill: colors.textSecondary }} />
                            <YAxis tick={{ fontSize: 12, fill: colors.textSecondary }} />
                            <Tooltip />
                            <Bar dataKey="margin" fill={colors.accent} radius={[4, 4, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            )}

            <div>
                {games.map((game) => (
                    <Link
                        key={game.id}
                        to={`/games/${game.id}`}
                        style={{ textDecoration: "none" }}
                    >
                        <div
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                padding: `${spacing.sm}px 0`,
                                borderBottom: `1px solid ${colors.border}`,
                                cursor: "pointer",
                            }}
                        >
                            <span style={{ color: colors.textPrimary }}>
                                {game.awayTeam.abbreviation} @ {game.homeTeam.abbreviation}
                            </span>
                            <span style={{ color: colors.textSecondary }}>
                                {game.status === "FINAL" ? `${game.awayScore} - ${game.homeScore}` : game.status}
                            </span>
                        </div>
                    </Link>
                ))}
            </div>
        </div>
    );
}

const buttonStyle: React.CSSProperties = {
    border: `1px solid ${colors.border}`,
    borderRadius: 6,
    padding: `${spacing.sm}px ${spacing.md}px`,
    background: "none",
    cursor: "pointer",
    color: colors.textPrimary,
    fontSize: 14,
};

export default GamesPage;

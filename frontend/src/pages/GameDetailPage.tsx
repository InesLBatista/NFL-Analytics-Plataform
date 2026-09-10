import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { apiClient, ApiError } from "../apiClient";
import type { Game, GameReport, GamePrediction, GameStats, PlayerStats } from "../types";
import { colors, spacing } from "../theme";
import LoadingView from "../components/LoadingView";
import ErrorView from "../components/ErrorView";

function GameDetailPage() {
    const { gameId } = useParams<{ gameId: string }>();

    const [game, setGame] = useState<Game | null>(null);
    const [report, setReport] = useState<GameReport | null>(null);
    const [prediction, setPrediction] = useState<GamePrediction | null>(null);
    const [teamStats, setTeamStats] = useState<GameStats[]>([]);
    const [playerStats, setPlayerStats] = useState<PlayerStats[]>([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!gameId) return;

        setLoading(true);
        setError(null);

        
        apiClient
            .get<Game>(`/api/games/${gameId}`)
            .then((gameData) => {
                setGame(gameData);
                setLoading(false);
            })
            .catch((err: ApiError) => {
                setError(err.message);
                setLoading(false);
            });

        apiClient.get<GameReport>(`/api/games/${gameId}/report`).then(setReport).catch(() => setReport(null));
        apiClient.get<GamePrediction>(`/api/games/${gameId}/prediction`).then(setPrediction).catch(() => setPrediction(null));
        apiClient.get<GameStats[]>(`/api/game-stats/game/${gameId}`).then(setTeamStats).catch(() => setTeamStats([]));
        apiClient.get<PlayerStats[]>(`/api/player-stats/game/${gameId}`).then(setPlayerStats).catch(() => setPlayerStats([]));
    }, [gameId]);

    if (loading) return <LoadingView message="Loading game..." />;
    if (error) return <ErrorView message={error} />;
    if (!game) return <ErrorView message="Game not found" />;

    const topPlayers = [...playerStats]
        .sort((a, b) => totalYards(b) - totalYards(a))
        .slice(0, 5);

    const teamStatsChartData = teamStats.map((ts) => ({
        team: ts.team.abbreviation,
        totalYards: ts.totalYards ?? 0,
    }));

    return (
        <div>
            {/* Cabeçalho */}
            <h1 style={{ fontSize: 24, color: colors.textPrimary, marginBottom: 4 }}>
                {game.awayTeam.name} @ {game.homeTeam.name}
            </h1>
            <p style={{ fontSize: 14, color: colors.textSecondary, marginBottom: spacing.sm }}>
                Season {game.season} — Week {game.week}
                {game.stadium && ` • ${game.stadium}`}
            </p>

            {game.status === "FINAL" ? (
                <div style={{ fontSize: 32, fontWeight: 700, color: colors.textPrimary, marginBottom: spacing.xl }}>
                    {game.awayTeam.abbreviation} {game.awayScore} — {game.homeScore} {game.homeTeam.abbreviation}
                </div>
            ) : (
                <div style={{ fontSize: 16, color: colors.textSecondary, marginBottom: spacing.xl }}>
                    {game.status}
                </div>
            )}

            {/* Previsão Elo */}
            {prediction && (
                <div
                    style={{
                        border: `1px solid ${colors.border}`,
                        borderRadius: 8,
                        padding: spacing.md,
                        backgroundColor: colors.surface,
                        marginBottom: spacing.xl,
                    }}
                >
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        Model prediction
                    </h3>
                    <PredictionBar
                        homeAbbr={game.homeTeam.abbreviation}
                        awayAbbr={game.awayTeam.abbreviation}
                        homeWinProbability={prediction.homeWinProbability}
                    />
                    {prediction.predictionCorrect !== null && (
                        <p style={{ fontSize: 12, color: colors.textSecondary, marginTop: spacing.sm }}>
                            Prediction was {prediction.predictionCorrect ? "correct ✓" : "incorrect ✗"}
                        </p>
                    )}
                </div>
            )}

            {/* Stats de equipa */}
            {teamStatsChartData.length > 0 && (
                <div style={{ marginBottom: spacing.xl }}>
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        Total yards by team
                    </h3>
                    <ResponsiveContainer width="100%" height={200}>
                        <BarChart data={teamStatsChartData} layout="vertical">
                            <CartesianGrid stroke={colors.border} horizontal={false} />
                            <XAxis type="number" tick={{ fontSize: 12, fill: colors.textSecondary }} />
                            <YAxis dataKey="team" type="category" tick={{ fontSize: 12, fill: colors.textSecondary }} width={50} />
                            <Tooltip />
                            <Bar dataKey="totalYards" fill={colors.accent} radius={[0, 4, 4, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            )}

            {/* Top jogadores */}
            {topPlayers.length > 0 && (
                <div style={{ marginBottom: spacing.xl }}>
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        Top performers
                    </h3>
                    {topPlayers.map((ps) => (
                        <div
                            key={ps.id}
                            style={{
                                padding: `${spacing.sm}px 0`,
                                borderBottom: `1px solid ${colors.border}`,
                                fontSize: 13,
                            }}
                        >
                            <span style={{ fontWeight: 600, color: colors.textPrimary }}>
                                {ps.player.fullName}
                            </span>
                            <span style={{ color: colors.textSecondary }}> ({ps.player.position}) — </span>
                            <span style={{ color: colors.textSecondary }}>{formatPlayerLine(ps)}</span>
                        </div>
                    ))}
                </div>
            )}

            {/* Relatório de IA */}
            <div>
                <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                    Game recap
                </h3>
                {report ? (
                    <p style={{ fontSize: 14, lineHeight: 1.6, color: colors.textPrimary, whiteSpace: "pre-line" }}>
                        {report.content}
                    </p>
                ) : (
                    <p style={{ fontSize: 13, color: colors.textSecondary }}>
                        No recap generated yet for this game.
                    </p>
                )}
            </div>
        </div>
    );
}



interface PredictionBarProps {
    homeAbbr: string;
    awayAbbr: string;
    homeWinProbability: number;
}

function PredictionBar({ homeAbbr, awayAbbr, homeWinProbability }: PredictionBarProps) {
    const homePct = Math.round(homeWinProbability * 100);
    const awayPct = 100 - homePct;

    return (
        <div>
            <div style={{ display: "flex", justifyContent: "space-between", fontSize: 13, marginBottom: 4 }}>
                <span style={{ color: colors.textPrimary }}>{awayAbbr} {awayPct}%</span>
                <span style={{ color: colors.textPrimary }}>{homeAbbr} {homePct}%</span>
            </div>
            <div style={{ display: "flex", height: 8, borderRadius: 4, overflow: "hidden", backgroundColor: colors.border }}>
                <div style={{ width: `${awayPct}%`, backgroundColor: colors.textSecondary }} />
                <div style={{ width: `${homePct}%`, backgroundColor: colors.accent }} />
            </div>
        </div>
    );
}

function totalYards(ps: PlayerStats): number {
    return (ps.passingYards ?? 0) + (ps.rushingYards ?? 0) + (ps.receivingYards ?? 0);
}

function formatPlayerLine(ps: PlayerStats): string {
    const parts: string[] = [];
    if (ps.passingYards) parts.push(`${ps.passingYards} pass yds, ${ps.passingTouchdowns ?? 0} TD`);
    if (ps.rushingYards) parts.push(`${ps.rushingYards} rush yds, ${ps.rushingTouchdowns ?? 0} TD`);
    if (ps.receivingYards) parts.push(`${ps.receptions ?? 0} rec, ${ps.receivingYards} yds, ${ps.receivingTouchdowns ?? 0} TD`);
    return parts.join(" • ");
}

export default GameDetailPage;
import { useState, useEffect, useMemo } from "react";
import { useParams, Link } from "react-router-dom";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { apiClient, ApiError } from "../apiClient";
import type { Team, Game, DraftPick, Trade } from "../types";
import { colors, spacing } from "../theme";
import LoadingView from "../components/LoadingView";
import ErrorView from "../components/ErrorView";

function TeamDetailPage() {
    const { abbreviation } = useParams<{ abbreviation: string }>();

    const [team, setTeam] = useState<Team | null>(null);
    const [allGames, setAllGames] = useState<Game[]>([]);
    const [draftPicks, setDraftPicks] = useState<DraftPick[]>([]);
    const [trades, setTrades] = useState<Trade[]>([]);

    const [season, setSeason] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!abbreviation) return;

        setLoading(true);
        setError(null);

        apiClient
            .get<Team>(`/api/teams/${abbreviation}`)
            .then((teamData) => {
                setTeam(teamData);
                setLoading(false);
            })
            .catch((err: ApiError) => {
                setError(err.message);
                setLoading(false);
            });
    }, [abbreviation]);


    
    useEffect(() => {
        if (!abbreviation) return;

      
        
        const defaultSeason = season ?? 2024;

        apiClient
            .get<Game[]>(`/api/games/team/${abbreviation}?season=${defaultSeason}`)
            .then((games) => {
                setAllGames(games);
                if (season === null) setSeason(defaultSeason);
            })
            .catch(() => setAllGames([]));
    }, [abbreviation, season]);

    useEffect(() => {
        if (!team || season === null) return;

        apiClient
            .get<DraftPick[]>(`/api/draft-picks/team/${team.id}?season=${season}`)
            .then(setDraftPicks)
            .catch(() => setDraftPicks([]));

        apiClient
            .get<Trade[]>(`/api/trades/team/${team.abbreviation}?season=${season}`)
            .then(setTrades)
            .catch(() => setTrades([]));
    }, [team, season]);

    const record = useMemo(() => {
        let wins = 0, losses = 0, ties = 0;

        for (const g of allGames) {
            if (g.status !== "FINAL" || g.homeScore === null || g.awayScore === null) continue;

            const isHome = g.homeTeam.abbreviation === abbreviation;
            const teamScore = isHome ? g.homeScore : g.awayScore;
            const oppScore = isHome ? g.awayScore : g.homeScore;

            if (teamScore > oppScore) wins++;
            else if (teamScore < oppScore) losses++;
            else ties++;
        }

        return { wins, losses, ties };
    }, [allGames, abbreviation]);

    const marginChartData = useMemo(() => {
        return [...allGames]
            .filter((g) => g.status === "FINAL")
            .sort((a, b) => a.week - b.week)
            .map((g) => {
                const isHome = g.homeTeam.abbreviation === abbreviation;
                const teamScore = isHome ? g.homeScore! : g.awayScore!;
                const oppScore = isHome ? g.awayScore! : g.homeScore!;
                return { week: `W${g.week}`, margin: teamScore - oppScore };
            });
    }, [allGames, abbreviation]);

    if (loading) return <LoadingView message="Loading team..." />;
    if (error) return <ErrorView message={error} />;
    if (!team) return <ErrorView message="Team not found" />;

    return (
        <div>
            <h1 style={{ fontSize: 24, color: colors.textPrimary, marginBottom: 4 }}>
                {team.name}
            </h1>
            <p style={{ fontSize: 14, color: colors.textSecondary, marginBottom: spacing.lg }}>
                {team.conference} {team.division}
            </p>

            <select
                value={season ?? ""}
                onChange={(e) => setSeason(Number(e.target.value))}
                style={{
                    padding: `${spacing.sm}px ${spacing.md}px`,
                    border: `1px solid ${colors.border}`,
                    borderRadius: 6,
                    fontSize: 14,
                    marginBottom: spacing.lg,
                }}
            >
                {/* Lista simples de épocas recentes -- ajusta consoante o histórico que tens importado */}
                {[2024, 2023, 2022, 2021].map((s) => (
                    <option key={s} value={s}>{s} Season</option>
                ))}
            </select>

            {/* Registo da época */}
            <div
                style={{
                    display: "grid",
                    gridTemplateColumns: "repeat(3, 1fr)",
                    gap: spacing.md,
                    marginBottom: spacing.xl,
                    maxWidth: 300,
                }}
            >
                <StatCard label="Wins" value={record.wins} />
                <StatCard label="Losses" value={record.losses} />
                <StatCard label="Ties" value={record.ties} />
            </div>

            {/* Gráfico de margem por semana */}
            {marginChartData.length > 0 && (
                <div style={{ marginBottom: spacing.xl }}>
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        Point margin by week
                    </h3>
                    <ResponsiveContainer width="100%" height={220}>
                        <LineChart data={marginChartData}>
                            <CartesianGrid stroke={colors.border} vertical={false} />
                            <XAxis dataKey="week" tick={{ fontSize: 12, fill: colors.textSecondary }} />
                            <YAxis tick={{ fontSize: 12, fill: colors.textSecondary }} />
                            <Tooltip />
                            <Line type="monotone" dataKey="margin" stroke={colors.accent} strokeWidth={2} dot={{ r: 3 }} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            )}

            {/* Jogos da época, com link para detalhe */}
            <div style={{ marginBottom: spacing.xl }}>
                <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                    Games
                </h3>
                {allGames.map((game) => (
                    <Link key={game.id} to={`/games/${game.id}`} style={{ textDecoration: "none" }}>
                        <div
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                padding: `${spacing.sm}px 0`,
                                borderBottom: `1px solid ${colors.border}`,
                                fontSize: 13,
                            }}
                        >
                            <span style={{ color: colors.textPrimary }}>
                                Week {game.week}: {game.awayTeam.abbreviation} @ {game.homeTeam.abbreviation}
                            </span>
                            <span style={{ color: colors.textSecondary }}>
                                {game.status === "FINAL" ? `${game.awayScore} - ${game.homeScore}` : game.status}
                            </span>
                        </div>
                    </Link>
                ))}
            </div>

            {/* Draft picks da época */}
            {draftPicks.length > 0 && (
                <div style={{ marginBottom: spacing.xl }}>
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        {season} Draft Picks
                    </h3>
                    {draftPicks.map((pick) => (
                        <div key={pick.id} style={{ fontSize: 13, color: colors.textSecondary, padding: `${spacing.xs}px 0` }}>
                            Round {pick.round}, Pick {pick.pick}: {pick.playerName} ({pick.position ?? "?"}, {pick.college ?? "N/A"})
                        </div>
                    ))}
                </div>
            )}

            {/* Trocas da época */}
            {trades.length > 0 && (
                <div>
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        {season} Trades
                    </h3>
                    {trades.map((trade) => {
                        const gave = trade.teamGiving === team.abbreviation;
                        return (
                            <div key={trade.id} style={{ fontSize: 13, color: colors.textSecondary, padding: `${spacing.xs}px 0` }}>
                                {gave ? "Sent" : "Received"} {trade.assetDescription} {gave ? "to" : "from"}{" "}
                                {gave ? trade.teamReceiving : trade.teamGiving}
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

interface StatCardProps {
    label: string;
    value: number;
}

function StatCard({ label, value }: StatCardProps) {
    return (
        <div
            style={{
                border: `1px solid ${colors.border}`,
                borderRadius: 8,
                padding: spacing.md,
                backgroundColor: colors.surface,
                textAlign: "center",
            }}
        >
            <div style={{ fontSize: 20, fontWeight: 700, color: colors.textPrimary }}>{value}</div>
            <div style={{ fontSize: 12, color: colors.textSecondary, marginTop: 2 }}>{label}</div>
        </div>
    );
}

export default TeamDetailPage;
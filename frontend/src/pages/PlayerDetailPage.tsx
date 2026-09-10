import { useState, useEffect, useMemo } from "react";
import { useParams } from "react-router-dom";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { apiClient, ApiError } from "../apiClient";
import type { Player, PlayerStats, Contract } from "../types";
import { colors, spacing } from "../theme";
import LoadingView from "../components/LoadingView";
import ErrorView from "../components/ErrorView";

function PlayerDetailPage() {
    const { playerId } = useParams<{ playerId: string }>();

    const [player, setPlayer] = useState<Player | null>(null);
    const [allStats, setAllStats] = useState<PlayerStats[]>([]);
    const [activeContracts, setActiveContracts] = useState<Contract[]>([]);
    const [usageDrops, setUsageDrops] = useState<string[]>([]);
    const [gamesMissed, setGamesMissed] = useState<number | null>(null);
    const [avgSnapPct, setAvgSnapPct] = useState<number | null>(null);

    const [season, setSeason] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    

    useEffect(() => {
        if (!playerId) return;

        setLoading(true);
        setError(null);

        Promise.all([
            apiClient.get<Player>(`/api/players/${playerId}`),
            apiClient.get<PlayerStats[]>(`/api/player-stats/player/${playerId}`),
        ])
            .then(([playerData, statsData]) => {
                setPlayer(playerData);
                setAllStats(statsData);

                const seasons = [...new Set(statsData.map((s) => s.game.season))].sort((a, b) => b - a);
                if (seasons.length > 0) setSeason(seasons[0]); // por defeito, a época mais recente

                setLoading(false);
            })
            .catch((err: ApiError) => {
                setError(err.message);
                setLoading(false);
            });
    }, [playerId]);

    
    useEffect(() => {
        if (!playerId || season === null) return;

        apiClient
            .get<number>(`/api/players/${playerId}/games-missed?season=${season}`)
            .then(setGamesMissed)
            .catch(() => setGamesMissed(null));

        apiClient
            .get<number>(`/api/players/${playerId}/usage/average?season=${season}`)
            .then(setAvgSnapPct)
            .catch(() => setAvgSnapPct(null));

        apiClient
            .get<string[]>(`/api/players/${playerId}/usage/drops?season=${season}`)
            .then(setUsageDrops)
            .catch(() => setUsageDrops([]));
    }, [playerId, season]);

    //active contract loads all once not dependent on the season
    useEffect(() => {
        if (!playerId) return;

        apiClient
            .get<Contract[]>(`/api/players/${playerId}/contracts/active`)
            .then(setActiveContracts)
            .catch(() => setActiveContracts([]));
    }, [playerId]);

    const availableSeasons = useMemo(
        () => [...new Set(allStats.map((s) => s.game.season))].sort((a, b) => b - a),
        [allStats]
    );

    const seasonStats = useMemo(
        () => allStats.filter((s) => s.game.season === season).sort((a, b) => a.game.week - b.game.week),
        [allStats, season]
    );

    const totals = useMemo(() => {
        return seasonStats.reduce(
            (acc, s) => ({
                passingYards: acc.passingYards + (s.passingYards ?? 0),
                rushingYards: acc.rushingYards + (s.rushingYards ?? 0),
                receivingYards: acc.receivingYards + (s.receivingYards ?? 0),
                touchdowns:
                    acc.touchdowns +
                    (s.passingTouchdowns ?? 0) +
                    (s.rushingTouchdowns ?? 0) +
                    (s.receivingTouchdowns ?? 0),
            }),
            { passingYards: 0, rushingYards: 0, receivingYards: 0, touchdowns: 0 }
        );
    }, [seasonStats]);

    const weeklyYardsData = seasonStats.map((s) => ({
        week: `W${s.game.week}`,
        yards: (s.passingYards ?? 0) + (s.rushingYards ?? 0) + (s.receivingYards ?? 0),
    }));

    if (loading) return <LoadingView message="Loading player..." />;
    if (error) return <ErrorView message={error} />;
    if (!player) return <ErrorView message="Player not found" />;

    return (
        <div>
            <h1 style={{ fontSize: 24, color: colors.textPrimary, marginBottom: 4 }}>
                {player.fullName}
            </h1>
            <p style={{ fontSize: 14, color: colors.textSecondary, marginBottom: spacing.lg }}>
                {player.position}
                {player.jerseyNumber !== null && ` • #${player.jerseyNumber}`}
                {player.team && ` • ${player.team.name}`}
            </p>

            {availableSeasons.length > 0 && (
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
                    {availableSeasons.map((s) => (
                        <option key={s} value={s}>
                            {s} Season
                        </option>
                    ))}
                </select>
            )}

            {/* Cards de resumo */}
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(140px, 1fr))", gap: spacing.md, marginBottom: spacing.xl }}>
                <StatCard label="Games played" value={seasonStats.length} />
                <StatCard label="Total yards" value={totals.passingYards + totals.rushingYards + totals.receivingYards} />
                <StatCard label="Touchdowns" value={totals.touchdowns} />
                <StatCard
                    label="Avg. offensive snaps"
                    value={avgSnapPct !== null ? `${avgSnapPct.toFixed(0)}%` : "N/A"}
                />
                <StatCard label="Games missed (injury)" value={gamesMissed ?? "N/A"} />
            </div>

            {/* Gráfico: yards totais por semana */}
            {weeklyYardsData.length > 0 && (
                <div style={{ marginBottom: spacing.xl }}>
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        Total yards per week
                    </h3>
                    <ResponsiveContainer width="100%" height={220}>
                        <BarChart data={weeklyYardsData}>
                            <CartesianGrid stroke={colors.border} vertical={false} />
                            <XAxis dataKey="week" tick={{ fontSize: 12, fill: colors.textSecondary }} />
                            <YAxis tick={{ fontSize: 12, fill: colors.textSecondary }} />
                            <Tooltip />
                            <Bar dataKey="yards" fill={colors.accent} radius={[4, 4, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            )}

            {/* Quedas de utilização, se existirem */}
            {usageDrops.length > 0 && (
                <div style={{ marginBottom: spacing.xl }}>
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        Usage drops
                    </h3>
                    {usageDrops.map((drop, i) => (
                        <div key={i} style={{ fontSize: 13, color: colors.textSecondary, marginBottom: 4 }}>
                            • {drop}
                        </div>
                    ))}
                </div>
            )}

            {/* Contrato ativo */}
            {activeContracts.length > 0 && (
                <div>
                    <h3 style={{ fontSize: 14, fontWeight: 600, color: colors.textPrimary, marginBottom: spacing.sm }}>
                        Contract
                    </h3>
                    {activeContracts.map((c) => (
                        <div
                            key={c.id}
                            style={{
                                border: `1px solid ${colors.border}`,
                                borderRadius: 8,
                                padding: spacing.md,
                                backgroundColor: colors.surface,
                                fontSize: 13,
                                color: colors.textSecondary,
                            }}
                        >
                            Signed {c.yearSigned} • {c.years} years
                            {c.apy !== null && ` • $${(c.apy / 1_000_000).toFixed(1)}M/year`}
                            {c.guaranteedMoney !== null && ` • $${(c.guaranteedMoney / 1_000_000).toFixed(1)}M guaranteed`}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

interface StatCardProps {
    label: string;
    value: string | number;
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

export default PlayerDetailPage;
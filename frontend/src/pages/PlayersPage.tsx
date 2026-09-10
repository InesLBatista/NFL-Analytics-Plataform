import { useState, useEffect } from "react";
import { apiClient, ApiError } from "../apiClient";
import type { Player, Team } from "../types";
import { colors, spacing } from "../theme";
import { useDebouncedValue } from "../hooks/useDebouncedValue";
import { Link } from "react-router-dom";
import LoadingView from "../components/LoadingView";
import ErrorView from "../components/ErrorView";

function PlayersPage() {
    const [searchInput, setSearchInput] = useState("");
    const debouncedSearch = useDebouncedValue(searchInput, 400);

    const [teams, setTeams] = useState<Team[]>([]);
    const [selectedTeam, setSelectedTeam] = useState<string>("");

    const [players, setPlayers] = useState<Player[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [hasSearched, setHasSearched] = useState(false);

    //loads the complete teams list once for the dropdown filter
    useEffect(() => {
        apiClient.get<Team[]>("/api/teams").then(setTeams).catch(() => {
            //not critical
        });
    }, []);

    //always when the debouced text or selected team changes
    useEffect(() => {
        if (debouncedSearch.trim() === "" && selectedTeam === "") {
            setPlayers([]);
            setHasSearched(false);
            return;
        }

        setLoading(true);
        setError(null);
        setHasSearched(true);

        const endpoint = selectedTeam
            ? `/api/players/team/${selectedTeam}`
            : `/api/players/search?name=${encodeURIComponent(debouncedSearch)}`;

        apiClient
            .get<Player[]>(endpoint)
            .then((data) => {
                setPlayers(data);
                setLoading(false);
            })
            .catch((err: ApiError) => {
                setError(err.message);
                setLoading(false);
            });
    }, [debouncedSearch, selectedTeam]);

    return (
        <div>
            <h1 style={{ fontSize: 22, color: colors.textPrimary, marginBottom: spacing.md }}>
                Players
            </h1>

            <div style={{ display: "flex", gap: spacing.md, marginBottom: spacing.lg, flexWrap: "wrap" }}>
                <input
                    type="text"
                    placeholder="Search by name (e.g. Mahomes)"
                    value={searchInput}
                    onChange={(e) => {
                        setSearchInput(e.target.value);
                        setSelectedTeam(""); 
                    }}
                    style={{
                        flex: 1,
                        minWidth: 220,
                        padding: `${spacing.sm}px ${spacing.md}px`,
                        border: `1px solid ${colors.border}`,
                        borderRadius: 6,
                        fontSize: 14,
                        outline: "none",
                    }}
                />

                <select
                    value={selectedTeam}
                    onChange={(e) => {
                        setSelectedTeam(e.target.value);
                        setSearchInput("");
                    }}
                    style={{
                        padding: `${spacing.sm}px ${spacing.md}px`,
                        border: `1px solid ${colors.border}`,
                        borderRadius: 6,
                        fontSize: 14,
                        color: colors.textPrimary,
                        backgroundColor: colors.background,
                    }}
                >
                    <option value="">Filter by team...</option>
                    {teams.map((team) => (
                        <option key={team.id} value={team.abbreviation}>
                            {team.name}
                        </option>
                    ))}
                </select>
            </div>

            {!hasSearched && (
                <p style={{ color: colors.textSecondary, fontSize: 14 }}>
                    Search by name or select a team to see players.
                </p>
            )}

            {loading && <LoadingView message="Searching players..." />}
            {error && <ErrorView message={error} />}

            {!loading && !error && hasSearched && players.length === 0 && (
                <p style={{ color: colors.textSecondary, fontSize: 14 }}>No players found.</p>
            )}

            {!loading && !error && players.length > 0 && (
                <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", gap: spacing.md }}>
                    {players.map((player) => (
                        <Link
                            key={player.id}
                            to={`/players/${player.id}`}
                            style={{ textDecoration: "none" }}
                        >
                            <div
                                style={{
                                    border: `1px solid ${colors.border}`,
                                    borderRadius: 8,
                                    padding: spacing.md,
                                    backgroundColor: colors.surface,
                                    cursor: "pointer",
                                }}
                            >
                                <div style={{ fontWeight: 600, color: colors.textPrimary }}>
                                    {player.fullName}
                                </div>
                                <div style={{ fontSize: 13, color: colors.textSecondary, marginTop: 4 }}>
                                    {player.position}
                                    {player.jerseyNumber !== null && ` • #${player.jerseyNumber}`}
                                </div>
                                {player.team && (
                                    <div style={{ fontSize: 13, color: colors.textSecondary, marginTop: 2 }}>
                                        {player.team.name}
                                    </div>
                                )}
                            </div>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}

export default PlayersPage;

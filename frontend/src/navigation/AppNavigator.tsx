import { NavLink, Route, Routes } from "react-router-dom";
import { BrowserRouter } from "react-router-dom";
import TeamsPage from "../pages/TeamPage";
import GamesPage from "../pages/GamesPage";
import PlayersPage from "../pages/PlayersPage";
import AssistantPage from "../pages/AssistantPage";
import { colors, spacing } from "../theme";

// tab icons as simple unicode/emoji — no icon library dependency required
const TAB_ICONS: Record<string, string> = {
    Teams: "🛡️",
    Games: "🏈",
    Players: "👤",
    Assistant: "💬",
};

const tabs = [
    { name: "Teams", path: "/", element: <TeamsPage /> },
    { name: "Games", path: "/games", element: <GamesPage /> },
    { name: "Players", path: "/players", element: <PlayersPage /> },
    { name: "Assistant", path: "/assistant", element: <AssistantPage /> },
];

function AppNavigator() {
    return (
        <BrowserRouter>
            <div style={{ display: "flex", flexDirection: "column", height: "100vh", backgroundColor: colors.background }}>

                {/* top header */}
                <header style={{ borderBottom: `1px solid ${colors.border}`, padding: `${spacing.sm}px ${spacing.md}px`, backgroundColor: colors.background }}>
                    <span style={{ fontWeight: 600, color: colors.textPrimary, fontSize: 16 }}>NFL Analytics</span>
                </header>

                {/* page content */}
                <main style={{ flex: 1, overflowY: "auto" }}>
                    <Routes>
                        {tabs.map((tab) => (
                            <Route key={tab.path} path={tab.path} element={tab.element} />
                        ))}
                    </Routes>
                </main>

                {/* bottom tab bar */}
                <nav style={{ display: "flex", borderTop: `1px solid ${colors.border}`, backgroundColor: colors.background }}>
                    {tabs.map((tab) => (
                        <NavLink
                            key={tab.path}
                            to={tab.path}
                            end={tab.path === "/"}
                            style={({ isActive }) => ({
                                flex: 1,
                                display: "flex",
                                flexDirection: "column",
                                alignItems: "center",
                                padding: `${spacing.sm}px 0`,
                                textDecoration: "none",
                                color: isActive ? colors.accent : colors.textSecondary,
                                fontSize: 11,
                                gap: 2,
                            })}
                        >
                            <span style={{ fontSize: 20 }}>{TAB_ICONS[tab.name]}</span>
                            <span>{tab.name}</span>
                        </NavLink>
                    ))}
                </nav>

            </div>
        </BrowserRouter>
    );
}

export default AppNavigator;

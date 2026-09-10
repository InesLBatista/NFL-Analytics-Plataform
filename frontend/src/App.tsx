import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./components/Layout";
import TeamsPage from "./pages/TeamPage";
import GamesPage from "./pages/GamesPage";
import PlayersPage from "./pages/PlayersPage";
import PlayerDetailPage from "./pages/PlayerDetailPage";
import AssistantPage from "./pages/AssistantPage";

function App() {
    return (
        <BrowserRouter>
            <Layout>
                <Routes>
                    <Route path="/" element={<TeamsPage />} />
                    <Route path="/games" element={<GamesPage />} />
                    <Route path="/players" element={<PlayersPage />} />
                    <Route path="/players/:playerId" element={<PlayerDetailPage />} />
                    <Route path="/assistant" element={<AssistantPage />} />
                </Routes>
            </Layout>
        </BrowserRouter>
    );
}

export default App;
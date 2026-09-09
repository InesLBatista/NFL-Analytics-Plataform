import ScreenContainer from "../components/ScreenContainer";
import { colors } from "../theme";

function PlayersPage() {
    return (
        <ScreenContainer>
            <h1 style={{ fontSize: 20, fontWeight: 600, color: colors.textPrimary }}>Players</h1>
        </ScreenContainer>
    );
}

export default PlayersPage;

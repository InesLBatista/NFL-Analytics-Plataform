import { ReactNode } from "react";
import Navbar from "./Navbar";
import Footer from "./Footer";
import { colors, spacing } from "../theme";

interface LayoutProps {
    children: ReactNode;
}

function Layout({ children }: LayoutProps) {
    return (
        <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column", backgroundColor: colors.background }}>
            <Navbar />
            <main style={{ flex: 1, maxWidth: 960, width: "100%", margin: "0 auto", padding: spacing.lg }}>
                {children}
            </main>
            <Footer />
        </div>
    );
}

export default Layout;
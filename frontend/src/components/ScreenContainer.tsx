import type { ReactNode } from "react";
import { colors, spacing } from "../theme";
import Footer from "./Footer";

// A interface Props documenta exatamente o que este componente aceita --
// se esqueceres um "children" obrigatório noutro sítio, o TypeScript avisa-te.
interface ScreenContainerProps {
    children: ReactNode;
    scrollable?: boolean;
}

function ScreenContainer({ children, scrollable = true }: ScreenContainerProps) {
    const content = (
        <div style={{ padding: spacing.md, backgroundColor: colors.background, flexGrow: 1 }}>
            {children}
            <Footer />
        </div>
    );

    if (!scrollable) {
        return (
            <div style={{ display: "flex", flexDirection: "column", height: "100%", backgroundColor: colors.background, overflow: "hidden" }}>
                {content}
            </div>
        );
    }

    return (
        <div style={{ flex: 1, backgroundColor: colors.background, overflowY: "auto" }}>
            {content}
        </div>
    );
}

export default ScreenContainer;

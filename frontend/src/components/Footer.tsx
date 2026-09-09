import { colors, spacing } from "../theme";

function Footer() {
    return (
        <div
            style={{
                borderTop: `1px solid ${colors.border}`,
                paddingTop: spacing.md,
                marginTop: spacing.lg,
                textAlign: "center",
            }}
        >
            <span style={{ color: colors.textSecondary, fontSize: 12 }}>
                Dados via nflverse • NFL Analytics Platform
            </span>
        </div>
    );
}

export default Footer;

import { colors, spacing } from "../theme";

function Footer() {
    return (
        <footer
            style={{
                borderTop: `1px solid ${colors.border}`,
                padding: `${spacing.md}px 0`,
                marginTop: spacing.xl,
                textAlign: "center",
                fontSize: 12,
                color: colors.textSecondary,
            }}
        >
            Data imported via nflverse - NFL Analytics Platform
        </footer>
    );
}

export default Footer;

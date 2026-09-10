import { NavLink } from "react-router-dom";
import { colors, spacing } from "../theme";

const links = [
    { to: "/", label: "Teams" },
    { to: "/games", label: "Games" },
    { to: "/players", label: "Players" },
    { to: "/assistant", label: "Assistant" },
];

function Navbar() {
    return (
        <nav
            style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                padding: `${spacing.md}px ${spacing.lg}px`,
                borderBottom: `1px solid ${colors.border}`,
                backgroundColor: colors.background,
            }}
        >
            <span style={{ fontWeight: 700, fontSize: 18, color: colors.textPrimary }}>
                NFL Analytics
            </span>

            <div style={{ display: "flex", gap: spacing.lg }}>
                {links.map((link) => (
                    <NavLink
                        key={link.to}
                        to={link.to}
                        style={({ isActive }) => ({
                            textDecoration: "none",
                            fontSize: 14,
                            fontWeight: isActive ? 600 : 400,
                            color: isActive ? colors.accent : colors.textSecondary,
                        })}
                    >
                        {link.label}
                    </NavLink>
                ))}
            </div>
        </nav>
    );
}

export default Navbar;
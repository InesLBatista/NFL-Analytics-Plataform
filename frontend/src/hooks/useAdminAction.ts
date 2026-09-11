import { useState } from "react";
import { ApiError } from "../apiClient";


export function useAdminAction() {
    const [pending, setPending] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function runAdminAction<T>(action: (credentials: { username: string; password: string }) => Promise<T>): Promise<T | null> {
        const username = window.prompt("Admin username:");
        if (!username) return null;

        const password = window.prompt("Admin password:");
        if (!password) return null;

        setPending(true);
        setError(null);

        try {
            const result = await action({ username, password });
            setPending(false);
            return result;
        } catch (err) {
            setError(err instanceof ApiError ? err.message : "Something went wrong.");
            setPending(false);
            return null;
        }
    }

    return { runAdminAction, pending, error };
}
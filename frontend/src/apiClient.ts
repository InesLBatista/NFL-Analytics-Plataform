//centralized base URL — uses Vite env variable, falls back to the local backend port
const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export class ApiError extends Error {
    status: number;
    constructor(message: string, status: number) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

interface RequestOptions extends RequestInit {
    auth?: { username: string; password: string };
}

//generic and typed wrapper over fetch. The type T is the expected response format
async function request<T>(path: string, options?: RequestOptions): Promise<T> {
    const headers = new Headers(options?.headers);

    if (options?.auth) {
        const encoded = btoa(`${options.auth.username}:${options.auth.password}`);
        headers.set("Authorization", `Basic ${encoded}`);
    }

    const response = await fetch(`${BASE_URL}${path}`, { ...options, headers });

    if (response.status === 401) {
        throw new ApiError("Invalid admin credentials.", 401);
    }

    if (!response.ok) {
        throw new ApiError(`Request failed: ${response.status}`, response.status);
    }

    return response.json() as Promise<T>;
}

export const apiClient = {
    get: <T>(path: string) => request<T>(path),
    post: <T>(path: string, body?: unknown, auth?: { username: string; password: string }) =>
        request<T>(path, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: body ? JSON.stringify(body) : undefined,
            auth,
        }),
};

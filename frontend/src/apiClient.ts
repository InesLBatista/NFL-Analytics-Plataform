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

//generic and typed wrapper over fetch. The type T is the expected response format
async function request<T>(path: string, options?: RequestInit): Promise<T> {
    const response = await fetch(`${BASE_URL}${path}`, options);

    if (!response.ok) {
        throw new ApiError(`Request failed: ${response.status}`, response.status);
    }

    return response.json() as Promise<T>;
}

export const apiClient = {
    get: <T>(path: string) => request<T>(path),
    post: <T>(path: string, body?: unknown) =>
        request<T>(path, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: body ? JSON.stringify(body) : undefined,
        }),
};

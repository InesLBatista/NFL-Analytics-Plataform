export interface Team {
    id: number;
    name: string;
    abbreviation: string;
    conference: string;
    division: string;
    city: string | null;
    logoUrl: string | null;
}

export type GameStatus = "SCHEDULED" | "IN_PROGRESS" | "FINAL";

export interface Game {
    id: number;
    homeTeam: Team;
    awayTeam: Team;
    gameDate: string;
    week: number;
    season: number;
    homeScore: number | null;
    awayScore: number | null;
    status: GameStatus;
    stadium: string | null;
    roof: string | null;
    surface: string | null;
    temp: number | null;
    wind: number | null;
    homeCoach: string | null;
    awayCoach: string | null;
}

export interface Player {
    id: number;
    fullName: string;
    position: string;
    jerseyNumber: number | null;
    team: Team | null;
    birthDate: string | null;
    heightCm: number | null;
    weightKg: number | null;
    externalId: string | null;
}

export interface PlayerStats {
    id: number;
    game: Game;
    player: Player;
    passingAttempts: number | null;
    passingCompletions: number | null;
    passingYards: number | null;
    passingTouchdowns: number | null;
    interceptions: number | null;
    rushingAttempts: number | null;
    rushingYards: number | null;
    rushingTouchdowns: number | null;
    targets: number | null;
    receptions: number | null;
    receivingYards: number | null;
    receivingTouchdowns: number | null;
    sacks: number | null;
    tackles: number | null;
    forcedFumbles: number | null;
}

export interface GameReport {
    id: number;
    game: Game;
    content: string;
    generatedAt: string;
}

export interface GamePrediction {
    id: number;
    game: Game;
    homeWinProbability: number;
    homeEloAtPrediction: number;
    awayEloAtPrediction: number;
    predictionCorrect: boolean | null;
}

export interface AssistantResponse {
    question: string;
    answer: string;
}

export interface Contract {
    id: number;
    player: Player | null;
    playerName: string;
    position: string | null;
    team: string | null;
    isActive: boolean;
    yearSigned: number;
    years: number | null;
    totalValue: number | null;
    apy: number | null;
    guaranteedMoney: number | null;
    apyCapPct: number | null;
    otcId: number | null;
}
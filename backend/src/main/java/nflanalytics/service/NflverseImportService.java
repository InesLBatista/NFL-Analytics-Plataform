package nflanalytics.service;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import nflanalytics.model.Game;
import nflanalytics.model.GameStats;
import nflanalytics.model.Player;
import nflanalytics.model.PlayerStats;
import nflanalytics.model.Team;
import nflanalytics.repository.GameRepository;
import nflanalytics.repository.GameStatsRepository;
import nflanalytics.repository.PlayerRepository;
import nflanalytics.repository.PlayerStatsRepository;
import nflanalytics.repository.TeamRepository;

@Service
@RequiredArgsConstructor
public class NflverseImportService {

    private static final Logger log = LoggerFactory.getLogger(NflverseImportService.class);

    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final GameStatsRepository gameStatsRepository;

    private static final String TEAMS_CSV_URL  = "https://github.com/nflverse/nflverse-data/releases/download/teams/teams_colors_logos.csv";
    private static final String GAMES_CSV_URL  = "https://github.com/nflverse/nflverse-data/releases/download/schedules/games.csv";
    private static final String PLAYERS_CSV_URL = "https://github.com/nflverse/nflverse-data/releases/download/players/players.csv";

    public void importTeams() throws Exception {
        log.info("Starting teams import from: {}", TEAMS_CSV_URL);
        try (CSVReader reader = openCsvFromUrl(TEAMS_CSV_URL)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);
            log.info("CSV header columns: {}", col.keySet());

            String[] row;
            int imported = 0;

            //track abbreviations and names already processed in this execution
            //nflverse has multiple historical abbreviations for the same team
            java.util.Set<String> processedAbbreviations = new java.util.HashSet<>();
            java.util.Set<String> processedNames         = new java.util.HashSet<>();

            while ((row = reader.readNext()) != null) {
                String abbreviation = row[col.get("team_abbr")];
                String name         = row[col.get("team_name")];

                boolean alreadyInDb      = teamRepository.findByAbbreviation(abbreviation) != null;
                boolean alreadySeenThisRun = processedAbbreviations.contains(abbreviation)
                        || processedNames.contains(name);

                if (alreadyInDb || alreadySeenThisRun) continue;

                processedAbbreviations.add(abbreviation);
                processedNames.add(name);

                Team team = new Team();
                team.setAbbreviation(abbreviation);
                team.setName(name);
                team.setConference(row[col.get("team_conf")]);
                team.setDivision(row[col.get("team_division")]);
                team.setLogoUrl(row[col.get("team_logo_espn")]);

                teamRepository.save(team);
                imported++;
            }
            log.info("Teams import finished. Imported: {}", imported);
        }
    }

    public void importGames(int fromSeason) throws Exception {
        try (CSVReader reader = openCsvFromUrl(GAMES_CSV_URL)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String[] row;
            int created = 0;
            int updated = 0;

            while ((row = reader.readNext()) != null) {
                int season = Integer.parseInt(row[col.get("season")]);
                if (season < fromSeason) continue;

                String homeAbbr = row[col.get("home_team")];
                String awayAbbr = row[col.get("away_team")];
                Team homeTeam = teamRepository.findByAbbreviation(homeAbbr);
                Team awayTeam = teamRepository.findByAbbreviation(awayAbbr);
                if (homeTeam == null || awayTeam == null) continue;

                Integer week = Integer.parseInt(row[col.get("week")]);

                Game game = gameRepository.findGameByTeams(season, week, homeAbbr, awayAbbr);
                boolean isNew = (game == null);
                if (isNew) {
                    game = new Game();
                    game.setHomeTeam(homeTeam);
                    game.setAwayTeam(awayTeam);
                    game.setSeason(season);
                    game.setWeek(week);
                }

                String gameday = getOrNull(row, col, "gameday");
                if (gameday != null) {
                    game.setGameDate(LocalDate.parse(gameday, dateFormat).atStartOfDay());
                }

                String homeScoreStr = getOrNull(row, col, "home_score");
                String awayScoreStr = getOrNull(row, col, "away_score");
                if (homeScoreStr != null && awayScoreStr != null) {
                    game.setHomeScore((int) Double.parseDouble(homeScoreStr));
                    game.setAwayScore((int) Double.parseDouble(awayScoreStr));
                    game.setStatus(Game.GameStatus.FINAL);
                } else {
                    game.setStatus(Game.GameStatus.SCHEDULED);
                }

                game.setStadium(getOrNull(row, col, "stadium"));
                game.setStadiumId(getOrNull(row, col, "stadium_id"));
                game.setRoof(getOrNull(row, col, "roof"));
                game.setSurface(getOrNull(row, col, "surface"));

                String tempStr = getOrNull(row, col, "temp");
                if (tempStr != null) {
                    try { game.setTemp((int) Double.parseDouble(tempStr)); } catch (NumberFormatException ignored) {}
                }
                String windStr = getOrNull(row, col, "wind");
                if (windStr != null) {
                    try { game.setWind((int) Double.parseDouble(windStr)); } catch (NumberFormatException ignored) {}
                }

                game.setHomeCoach(getOrNull(row, col, "home_coach"));
                game.setAwayCoach(getOrNull(row, col, "away_coach"));
                game.setHomeQbName(getOrNull(row, col, "home_qb_name"));
                game.setAwayQbName(getOrNull(row, col, "away_qb_name"));

                gameRepository.save(game);
                if (isNew) created++; else updated++;
            }
            log.info("Imported games: {} new, {} updated", created, updated);
        }
    }

    public void importPlayers() throws Exception {
        try (CSVReader reader = openCsvFromUrl(PLAYERS_CSV_URL)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);

            String[] row;
            int imported = 0;
            int skipped  = 0;

            while ((row = reader.readNext()) != null) {
                String gsisId = row[col.get("gsis_id")];

                // Many older players (pre-GSIS) don't have this field
                if (gsisId == null || gsisId.isBlank()) { skipped++; continue; }

                if (playerRepository.findByExternalId(gsisId) != null) continue;

                Player player = new Player();
                player.setExternalId(gsisId);
                player.setFullName(getOrNull(row, col, "display_name"));
                player.setPosition(getOrNull(row, col, "position"));

                String jersey = getOrNull(row, col, "jersey_number");
                if (jersey != null) {
                    try { player.setJerseyNumber((int) Double.parseDouble(jersey)); } catch (NumberFormatException ignored) {}
                }

                String birthDateStr = getOrNull(row, col, "birth_date");
                if (birthDateStr != null) {
                    try { player.setBirthDate(LocalDate.parse(birthDateStr)); } catch (Exception ignored) {}
                }

                // Height in inches → centimeters
                String heightStr = getOrNull(row, col, "height");
                if (heightStr != null) {
                    try { player.setHeightCm((int) Math.round(Double.parseDouble(heightStr) * 2.54)); }
                    catch (NumberFormatException ignored) {}
                }

                // Weight in pounds → kilograms
                String weightStr = getOrNull(row, col, "weight");
                if (weightStr != null) {
                    try { player.setWeightKg((int) Math.round(Double.parseDouble(weightStr) * 0.453592)); }
                    catch (NumberFormatException ignored) {}
                }

                String teamAbbr = getOrNull(row, col, "latest_team");
                if (teamAbbr != null) {
                    player.setTeam(teamRepository.findByAbbreviation(teamAbbr)); //null if doesn't exist, won't break
                }

                playerRepository.save(player);
                imported++;
            }
            log.info("Imported players: {} | skipped (without gsis_id): {}", imported, skipped);
        }
    }

    public void importPlayerStats(int season) throws Exception {
        String url = "https://github.com/nflverse/nflverse-data/releases/download/player_stats/player_stats_" + season + ".csv";
        try (CSVReader reader = openCsvFromUrl(url)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);
            log.info("PlayerStats CSV columns: {}", Arrays.toString(header));

            String[] row;
            int imported         = 0;
            int skippedNoPlayer  = 0;
            int skippedNoGame    = 0;

            while ((row = reader.readNext()) != null) {
                String gsisId = getOrNull(row, col, "player_id");
                if (gsisId == null) continue;

                Player player = playerRepository.findByExternalId(gsisId);
                if (player == null) { skippedNoPlayer++; continue; }

                String weekStr  = getOrNull(row, col, "week");
                String team     = getOrNull(row, col, "team");
                String opponent = getOrNull(row, col, "opponent_team");
                if (weekStr == null || team == null || opponent == null) continue;

                Integer week = (int) Double.parseDouble(weekStr);
                Game game = gameRepository.findGameByTeams(season, week, team, opponent);
                if (game == null) { skippedNoGame++; continue; }

                if (playerStatsRepository.existsByPlayer_IdAndGame_Id(player.getId(), game.getId())) continue;

                PlayerStats stats = new PlayerStats();
                stats.setPlayer(player);
                stats.setGame(game);
                stats.setPassingAttempts(parseIntSafe(getOrNull(row, col, "attempts")));
                stats.setPassingCompletions(parseIntSafe(getOrNull(row, col, "completions")));
                stats.setPassingYards(parseIntSafe(getOrNull(row, col, "passing_yards")));
                stats.setPassingTouchdowns(parseIntSafe(getOrNull(row, col, "passing_tds")));
                stats.setInterceptions(parseIntSafe(getOrNull(row, col, "interceptions")));
                stats.setRushingAttempts(parseIntSafe(getOrNull(row, col, "carries")));
                stats.setRushingYards(parseIntSafe(getOrNull(row, col, "rushing_yards")));
                stats.setRushingTouchdowns(parseIntSafe(getOrNull(row, col, "rushing_tds")));
                stats.setTargets(parseIntSafe(getOrNull(row, col, "targets")));
                stats.setReceptions(parseIntSafe(getOrNull(row, col, "receptions")));
                stats.setReceivingYards(parseIntSafe(getOrNull(row, col, "receiving_yards")));
                stats.setReceivingTouchdowns(parseIntSafe(getOrNull(row, col, "receiving_tds")));
                // Defensive columns may not exist in all seasons
                stats.setTackles(parseIntSafe(getOrNull(row, col, "def_tackles_solo")));
                stats.setSacks(parseIntSafe(getOrNull(row, col, "def_sacks")));
                stats.setForcedFumbles(parseIntSafe(getOrNull(row, col, "def_fumbles_forced")));

                playerStatsRepository.save(stats);
                imported++;
            }
            log.info("Imported PlayerStats: {} | no player: {} | no game: {}", imported, skippedNoPlayer, skippedNoGame);
        }
    }

    public void importGameStats(int season) throws Exception {
        String url = "https://github.com/nflverse/nflverse-data/releases/download/stats_team/stats_team_" + season + ".csv";
        try (CSVReader reader = openCsvFromUrl(url)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);
            log.info("GameStats CSV columns: {}", Arrays.toString(header));

            String[] row;
            int imported       = 0;
            int skippedNoTeam  = 0;
            int skippedNoGame  = 0;

            while ((row = reader.readNext()) != null) {
                String teamAbbr  = getOrNull(row, col, "team");
                String opponent  = getOrNull(row, col, "opponent_team");
                String weekStr   = getOrNull(row, col, "week");
                if (teamAbbr == null || opponent == null || weekStr == null) continue;

                Team team = teamRepository.findByAbbreviation(teamAbbr);
                if (team == null) { skippedNoTeam++; continue; }

                Integer week = (int) Double.parseDouble(weekStr);
                Game game = gameRepository.findGameByTeams(season, week, teamAbbr, opponent);
                if (game == null) { skippedNoGame++; continue; }

                if (gameStatsRepository.existsByGame_IdAndTeam_Id(game.getId(), team.getId())) continue;

                GameStats stats = new GameStats();
                stats.setGame(game);
                stats.setTeam(team);

                //sum of passing + rushing since the CSV doesn't always have total_yards
                Integer passingYards = parseIntSafe(getOrNull(row, col, "passing_yards"));
                Integer rushingYards = parseIntSafe(getOrNull(row, col, "rushing_yards"));
                stats.setPassingYards(passingYards);
                stats.setRushingYards(rushingYards);
                if (passingYards != null && rushingYards != null) {
                    stats.setTotalYards(passingYards + rushingYards);
                }

                //turnovers = interceptions + lost fumbles
                Integer interceptions = parseIntSafe(getOrNull(row, col, "interceptions"));
                Integer fumblesLost   = parseIntSafe(getOrNull(row, col, "rushing_fumbles_lost"));
                if (interceptions != null || fumblesLost != null) {
                    stats.setTurnovers(
                        (interceptions != null ? interceptions : 0) +
                        (fumblesLost   != null ? fumblesLost   : 0)
                    );
                }

                stats.setSacks(parseIntSafe(getOrNull(row, col, "sacks_suffered")));
                stats.setThirdDownConversions(parseIntSafe(getOrNull(row, col, "third_down_conversions")));
                stats.setThirdDownAttempts(parseIntSafe(getOrNull(row, col, "third_down_attempts")));
                stats.setPenalties(parseIntSafe(getOrNull(row, col, "penalties")));
                stats.setPenaltyYards(parseIntSafe(getOrNull(row, col, "penalty_yards")));

                gameStatsRepository.save(stats);
                imported++;
            }
            log.info("Imported GameStats: {} | no team: {} | no game: {}", imported, skippedNoTeam, skippedNoGame);
        }
    }

    //opens the CSV directly from the URL without saving to disk
    //manually follows redirects to have control over the flow
    private CSVReader openCsvFromUrl(String urlString) throws Exception {
        String currentUrl = urlString;
        for (int redirects = 0; redirects < 5; redirects++) {
            log.info("Fetching CSV from: {}", currentUrl);
            URL url = new URL(currentUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);

            int status = connection.getResponseCode();
            log.info("HTTP response: {}", status);

            if (status == HttpURLConnection.HTTP_OK) {
                return new CSVReader(new InputStreamReader(connection.getInputStream()));
            } else if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == 307 || status == 308) {
                currentUrl = connection.getHeaderField("Location");
                log.info("Redirected to: {}", currentUrl);
                connection.disconnect();
            } else {
                throw new RuntimeException("Failed to fetch CSV from " + currentUrl + " — HTTP " + status);
            }
        }
        throw new RuntimeException("Too many redirects for URL: " + urlString);
    }

    //builds a column-name to index map from the header
    private Map<String, Integer> mapColumns(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            map.put(header[i], i);
        }
        return map;
    }

    //returns null if value is missing, out of bounds, or blank
    private String getOrNull(String[] row, Map<String, Integer> col, String columnName) {
        Integer index = col.get(columnName);
        if (index == null || index >= row.length) return null;
        String value = row[index];
        return (value == null || value.isBlank()) ? null : value;
    }

    private Integer parseIntSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try { return (int) Double.parseDouble(value); } catch (NumberFormatException e) { return null; }
    }
}
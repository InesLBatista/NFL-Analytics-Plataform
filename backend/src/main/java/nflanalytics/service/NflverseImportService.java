package nflanalytics.service;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import nflanalytics.model.DraftPick;
import nflanalytics.model.Game;
import nflanalytics.model.GameStats;
import nflanalytics.model.Injury;
import nflanalytics.model.Official;
import nflanalytics.model.PlayByPlay;
import nflanalytics.model.Player;
import nflanalytics.model.PlayerStats;
import nflanalytics.model.SnapCount;
import nflanalytics.model.Team;
import nflanalytics.repository.DraftPickRepository;
import nflanalytics.repository.GameRepository;
import nflanalytics.repository.GameStatsRepository;
import nflanalytics.repository.InjuryRepository;
import nflanalytics.repository.OfficialRepository;
import nflanalytics.repository.PlayByPlayRepository;
import nflanalytics.repository.PlayerRepository;
import nflanalytics.repository.PlayerStatsRepository;
import nflanalytics.repository.SnapCountRepository;
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
    private final PlayByPlayRepository playByPlayRepository;
    private final DraftPickRepository draftPickRepository;
    private final OfficialRepository officialRepository;
    private final InjuryRepository injuryRepository;
    private final SnapCountRepository snapCountRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.persistence.EntityManager entityManager;

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

    public void importPlayByPlay(int season) throws Exception {
        long existing = playByPlayRepository.countBySeason(season);
        if (existing > 0) {
            System.out.println(season + "season already has" + existing + " imported plays. Skipping.");
            return;
        }

    
        List<Game> seasonGames = gameRepository.findBySeason(season);
        Map<String, Game> gameLookup = new HashMap<>();
        for (Game g : seasonGames) {
            String key = buildGameKey(g.getWeek(), g.getHomeTeam().getAbbreviation(), g.getAwayTeam().getAbbreviation());
            gameLookup.put(key, g);
        }
        System.out.println("Games pre-loaded for lookup: " + gameLookup.size());

        String url = "https://github.com/nflverse/nflverse-data/releases/download/pbp/play_by_play_" + season + ".csv";

        try (CSVReader reader = openCsvFromUrl(url)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);
            System.out.println("CSV header columns count: " + header.length);

            String[] row;
            int imported = 0;
            int skippedNoGame = 0;
            List<PlayByPlay> batch = new ArrayList<>();
            int batchSize = 500;

            while ((row = reader.readNext()) != null) {
                String homeTeam = getOrNull(row, col, "home_team");
                String awayTeam = getOrNull(row, col, "away_team");
                String weekStr = getOrNull(row, col, "week");
                if (homeTeam == null || awayTeam == null || weekStr == null) continue;

                Integer week = (int) Double.parseDouble(weekStr);
                String key = buildGameKey(week, homeTeam, awayTeam);
                Game game = gameLookup.get(key);
                if (game == null) {
                    skippedNoGame++;
                    continue;
                }

                PlayByPlay play = new PlayByPlay();
                play.setGame(game);
                play.setSeason(season);
                play.setWeek(week);

                String nflverseGameId = getOrNull(row, col, "game_id");
                String playId = getOrNull(row, col, "play_id");
                play.setExternalPlayId(nflverseGameId + "_" + playId);

                play.setQuarter(parseIntSafe(getOrNull(row, col, "qtr")));
                play.setDown(parseIntSafe(getOrNull(row, col, "down")));
                play.setYardsToGo(parseIntSafe(getOrNull(row, col, "ydstogo")));
                play.setYardlineNumber(parseIntSafe(getOrNull(row, col, "yardline_100")));
                play.setPlayType(getOrNull(row, col, "play_type"));
                play.setDescription(getOrNull(row, col, "desc"));
                play.setPosTeam(getOrNull(row, col, "posteam"));
                play.setDefTeam(getOrNull(row, col, "defteam"));
                play.setYardsGained(parseIntSafe(getOrNull(row, col, "yards_gained")));

                play.setEpa(parseDoubleSafe(getOrNull(row, col, "epa")));
                play.setWpa(parseDoubleSafe(getOrNull(row, col, "wpa")));
                play.setSuccess(parseBooleanSafe(getOrNull(row, col, "success")));
                play.setTouchdown(parseBooleanSafe(getOrNull(row, col, "touchdown")));
                play.setInterception(parseBooleanSafe(getOrNull(row, col, "interception")));
                play.setFumble(parseBooleanSafe(getOrNull(row, col, "fumble")));
                play.setSack(parseBooleanSafe(getOrNull(row, col, "sack")));
                play.setPenalty(parseBooleanSafe(getOrNull(row, col, "penalty")));

                play.setPasserName(getOrNull(row, col, "passer_player_name"));
                play.setRusherName(getOrNull(row, col, "rusher_player_name"));
                play.setReceiverName(getOrNull(row, col, "receiver_player_name"));

                play.setGameSecondsRemaining(parseIntSafe(getOrNull(row, col, "game_seconds_remaining")));
                play.setPosteamScore(parseIntSafe(getOrNull(row, col, "posteam_score")));
                play.setDefteamScore(parseIntSafe(getOrNull(row, col, "defteam_score")));

                batch.add(play);
                imported++;



                if (batch.size() >= batchSize) {
                    playByPlayRepository.saveAll(batch);
                    entityManager.flush();
                    entityManager.clear();
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                playByPlayRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            }

            System.out.println("Imported PlayByPlay: " + imported + " | without corresponding game: " + skippedNoGame);
        }
    }

    public void importDraftPicks() throws Exception {
        String url = "https://github.com/nflverse/nflverse-data/releases/download/draft_picks/draft_picks.csv";

        try (CSVReader reader = openCsvFromUrl(url)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);
            System.out.println("CSV header columns: " + Arrays.toString(header));

            String[] row;
            int imported = 0;

            while ((row = reader.readNext()) != null) {
                String seasonStr = getOrNull(row, col, "season");
                String roundStr = getOrNull(row, col, "round");
                String pickStr = getOrNull(row, col, "pick");
                if (seasonStr == null || roundStr == null || pickStr == null) continue;

                Integer season = parseIntSafe(seasonStr);
                Integer round = parseIntSafe(roundStr);
                Integer pick = parseIntSafe(pickStr);

                if (draftPickRepository.existsBySeasonAndRoundAndPick(season, round, pick)) continue;

                DraftPick dp = new DraftPick();
                dp.setSeason(season);
                dp.setRound(round);
                dp.setPick(pick);

                String teamAbbr = getOrNull(row, col, "team");
                if (teamAbbr != null) {
                    dp.setTeam(teamRepository.findByAbbreviation(teamAbbr));
                }

                String gsisId = getOrNull(row, col, "gsis_id");
                if (gsisId != null) {
                    dp.setPlayer(playerRepository.findByExternalId(gsisId));
                }

                dp.setPlayerName(getOrNull(row, col, "pfr_player_name"));
                dp.setPosition(getOrNull(row, col, "position"));
                dp.setCollege(getOrNull(row, col, "college"));

                draftPickRepository.save(dp);
                imported++;
            }

            System.out.println("Draft picks imported: " + imported);
        }
    }

    public void importOfficials(int season) throws Exception {
        String url = "https://github.com/nflverse/nflverse-data/releases/download/officials/officials.csv";

        try (CSVReader reader = openCsvFromUrl(url)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);
            System.out.println("CSV header columns: " + Arrays.toString(header));

            String[] row;
            int imported = 0;
            int skippedNoGame = 0;

            while ((row = reader.readNext()) != null) {
                String seasonStr = getOrNull(row, col, "season");
                if (seasonStr == null || !seasonStr.equals(String.valueOf(season))) continue;

                String homeTeam = getOrNull(row, col, "home_team");
                String awayTeam = getOrNull(row, col, "away_team");
                String weekStr = getOrNull(row, col, "week");
                if (homeTeam == null || awayTeam == null || weekStr == null) continue;

                Integer week = parseIntSafe(weekStr);
                Game game = gameRepository.findGameByTeams(season, week, homeTeam, awayTeam);
                if (game == null) {
                    skippedNoGame++;
                    continue;
                }

                String name = getOrNull(row, col, "official_name");
                String role = getOrNull(row, col, "official_position");
                if (name == null) continue;

                if (officialRepository.existsByGame_IdAndNameAndRole(game.getId(), name, role)) continue;

                Official official = new Official();
                official.setGame(game);
                official.setName(name);
                official.setRole(role);

                officialRepository.save(official);
                imported++;
            }

            System.out.println("Officials imported: " + imported + " | without corresponding game: " + skippedNoGame);
        }
    }

    public void importInjuries(int season) throws Exception {
        String url = "https://github.com/nflverse/nflverse-data/releases/download/injuries/injuries_" + season + ".csv";

        try (CSVReader reader = openCsvFromUrl(url)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);
            System.out.println("CSV header columns: " + Arrays.toString(header));

            String[] row;
            int imported = 0;
            int skippedNoGame = 0;

            while ((row = reader.readNext()) != null) {
                String weekStr = getOrNull(row, col, "week");
                String team = getOrNull(row, col, "team");
                String gsisId = getOrNull(row, col, "gsis_id");
                if (weekStr == null || team == null) continue;

                Integer week = parseIntSafe(weekStr);

                Player player = (gsisId != null) ? playerRepository.findByExternalId(gsisId) : null;

            
                if (player != null && injuryRepository.existsByPlayer_IdAndSeasonAndWeek(player.getId(), season, week)) {
                    continue;
                }

                Game game = gameRepository.findGameByTeamAndWeek(season, week, team);
                if (game == null) skippedNoGame++;

                Injury injury = new Injury();
                injury.setPlayer(player);
                injury.setGame(game); //null if not found
                injury.setSeason(season);
                injury.setWeek(week);
                injury.setTeam(team);
                injury.setReportPrimaryInjury(getOrNull(row, col, "report_primary_injury"));
                injury.setReportSecondaryInjury(getOrNull(row, col, "report_secondary_injury"));
                injury.setReportStatus(getOrNull(row, col, "report_status"));
                injury.setPracticePrimaryInjury(getOrNull(row, col, "practice_primary_injury"));
                injury.setPracticeStatus(getOrNull(row, col, "practice_status"));

                injuryRepository.save(injury);
                imported++;
            }

            System.out.println("Injuries imported: " + imported + " | without corresponding game: " + skippedNoGame);
        }
    }




    public void importSnapCounts(int season) throws Exception {
        String url = "https://github.com/nflverse/nflverse-data/releases/download/snap_counts/snap_counts_" + season + ".csv";

        try (CSVReader reader = openCsvFromUrl(url)) {
            String[] header = reader.readNext();
            Map<String, Integer> col = mapColumns(header);
            System.out.println("CSV header columns: " + Arrays.toString(header));

            String[] row;
            int imported = 0;
            int linkedToPlayer = 0;
            int linkedToGame = 0;

            while ((row = reader.readNext()) != null) {
                String playerName = getOrNull(row, col, "player");
                String team = getOrNull(row, col, "team");
                String weekStr = getOrNull(row, col, "week");
                if (playerName == null || team == null || weekStr == null) continue;

                Integer week = parseIntSafe(weekStr);

                if (snapCountRepository.existsByPlayerNameAndTeamAndSeasonAndWeek(playerName, team, season, week)) {
                    continue;
                }

                SnapCount sc = new SnapCount();
                sc.setPlayerName(playerName);
                sc.setTeam(team);
                sc.setSeason(season);
                sc.setWeek(week);
                sc.setPosition(getOrNull(row, col, "position"));

            

                Player player = playerRepository.findByFullNameIgnoreCaseAndTeam_Abbreviation(playerName, team);
                if (player != null) {
                    sc.setPlayer(player);
                    linkedToPlayer++;
                }

                Game game = gameRepository.findGameByTeamAndWeek(season, week, team);
                if (game != null) {
                    sc.setGame(game);
                    linkedToGame++;
                }

                sc.setOffenseSnaps(parseIntSafe(getOrNull(row, col, "offense_snaps")));
                sc.setOffensePct(parseDoubleSafe(getOrNull(row, col, "offense_pct")));
                sc.setDefenseSnaps(parseIntSafe(getOrNull(row, col, "defense_snaps")));
                sc.setDefensePct(parseDoubleSafe(getOrNull(row, col, "defense_pct")));
                sc.setSpecialTeamsSnaps(parseIntSafe(getOrNull(row, col, "st_snaps")));
                sc.setSpecialTeamsPct(parseDoubleSafe(getOrNull(row, col, "st_pct")));

                snapCountRepository.save(sc);
                imported++;
            }

            System.out.println("SnapCounts imported: " + imported + " | connected to Player: " + linkedToPlayer + " | connected to Game: " + linkedToGame);
        }
    }

    //chave para o lookup em memória de jogos 
    private String buildGameKey(Integer week, String team1, String team2) {
        String[] teams = { team1, team2 };
        java.util.Arrays.sort(teams);
        return week + "_" + teams[0] + "_" + teams[1];
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseBooleanSafe(String value) {
        if (value == null || value.isBlank()) return null;
        return value.equals("1") || value.equalsIgnoreCase("true");
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
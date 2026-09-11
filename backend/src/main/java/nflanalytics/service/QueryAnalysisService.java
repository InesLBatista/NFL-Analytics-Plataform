package nflanalytics.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

//extraction of structured filters from a question before vectorial search
//uses regex rules instead of calling the LLM API — avoids an extra API call per user question
@Service
public class QueryAnalysisService {

    public record QueryFilters(Integer season, String sourceType) {}

    // matches a 4-digit year in the range 1990–2099 (covers all NFL seasons likely to be in the DB)
    private static final Pattern SEASON_PATTERN = Pattern.compile("\\b(19[9][0-9]|20[0-9]{2})\\b");

    // keywords that suggest the question is about a specific game or match
    private static final Pattern GAME_PATTERN = Pattern.compile(
        "\\b(game|match|matchup|recap|report|score|quarter|half|overtime|ot|week \\d+)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // keywords that suggest the question is about a player
    private static final Pattern PLAYER_PATTERN = Pattern.compile(
        "\\b(player|quarterback|qb|running back|rb|wide receiver|wr|tight end|te|" +
        "linebacker|lb|cornerback|cb|safety|defensive end|de|offensive line|ol|" +
        "stats|statistics|yards|touchdowns|interceptions|sacks|snap|contract|salary|injury|injured)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // keywords that suggest the question is about a team as a whole
    private static final Pattern TEAM_PATTERN = Pattern.compile(
        "\\b(team|franchise|season record|win|loss|wins|losses|coach|coaching|" +
        "draft|trade|roster|offense|defense|special teams|standings|division|conference|playoff)\\b",
        Pattern.CASE_INSENSITIVE
    );

    public QueryFilters analyze(String question) {
        if (question == null || question.isBlank()) return new QueryFilters(null, null);

        // extract season year if explicitly mentioned
        Integer season = null;
        Matcher seasonMatcher = SEASON_PATTERN.matcher(question);
        if (seasonMatcher.find()) {
            try {
                season = Integer.parseInt(seasonMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        // determine source type by counting keyword matches per category
        // the category with the most matches wins; ties and empty results return null (no filter)
        int gameScore   = countMatches(GAME_PATTERN,   question);
        int playerScore = countMatches(PLAYER_PATTERN, question);
        int teamScore   = countMatches(TEAM_PATTERN,   question);

        String sourceType = null;
        int maxScore = Math.max(gameScore, Math.max(playerScore, teamScore));

        if (maxScore > 0) {
            if (gameScore == maxScore)        sourceType = "game_report";
            else if (playerScore == maxScore) sourceType = "player_season_summary";
            else                              sourceType = "team_season_summary";
        }

        return new QueryFilters(season, sourceType);
    }

    private int countMatches(Pattern pattern, String text) {
        int count = 0;
        Matcher m = pattern.matcher(text);
        while (m.find()) count++;
        return count;
    }
}

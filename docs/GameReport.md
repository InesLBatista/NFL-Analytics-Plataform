## Game Report Generation

The platform includes an automated AI-powered game report feature. After each NFL game, the system generates a written summary covering the most relevant events, standout performances, statistical highlights, and key turning points, without requiring any manual input. The reports are generated through a dedicated backend service that retrieves game data from the database, builds a structured prompt, and calls the Anthropic Claude API to produce the final text. Generated reports are persisted in the database linked to their game record, so subsequent requests for the same game return the stored result without triggering additional API calls.

The process begins when a report is requested for a specific game or for an entire week. The service aggregates all relevant data from the database, assembles it into a structured prompt, and instructs the model to write a 3 to 4 paragraph recap in Portuguese in a professional yet engaging tone, as if published on a sports website. The model is explicitly told not to invent statistics and to rely only on the data provided.

### Current Report Content

The prompt passed to the model currently includes the following data for each game:

- Game metadata: season, week, stadium, roof type, surface, temperature, and wind conditions
- Head coaches for both teams
- Team statistics per side: total offensive yards broken down into passing and rushing, turnovers, sacks conceded, third-down conversion rate, penalties, and penalty yards
- Top 5 players by combined total yards, with full stat lines split by category — completions and attempts, passing yards, touchdowns and interceptions for quarterbacks; carries, rushing yards, and touchdowns for running backs; targets, receptions, receiving yards, and touchdowns for receivers; and sack totals for defensive players
- The 6 most decisive plays of the game ranked by absolute Expected Points Added, each identified by quarter and time remaining, giving the model the sequence of momentum-shifting moments
- Injury report context split into two groups: players confirmed out with their primary and secondary injury designation, and players listed as Questionable, Doubtful, or Limited with their practice status, allowing the narrative to contextualise absences and unexpected lineup contributions

### Week-Level Generation

Rather than requiring a separate API call per game, the system supports generating reports for an entire week in a single request. The week-level endpoint fetches all games for the given season and week, skips any that already have a stored report, and processes the remaining ones sequentially. Each game is handled independently so that an API failure on one matchup does not abort the rest of the slate. A 1500 millisecond delay is applied between calls to stay within Anthropic's rate limits. The response includes a structured summary with counts of generated, failed, and skipped games, along with a detailed error message for each failure identifying the matchup and the reason.

### Endpoints

A stored report for a specific game can be retrieved with GET /api/games/{gameId}/report. Generating or regenerating a single game report is done with POST /api/admin/games/{gameId}/generate-report, which requires admin authentication. The week-level batch endpoint is POST /api/admin/reports/season/{season}/week/{week} and returns the WeekReportResult summary rather than the individual report content.

### Planned Improvements

The depth of the generated reports will continue to grow as additional data sources are integrated into the prompt. Snap count data will allow the narrative to include playing time context alongside raw statistics, which is particularly relevant for receivers and rotational players. Next Generation Stats such as average air yards, completion percentage above expectation, and yards after contact will add a layer of efficiency analysis beyond the traditional box score. Officials data will allow the report to note the officiating crew and flag games where penalty volume was unusually high. Draft background for players highlighted in the recap will add biographical context, particularly for rookies making notable contributions.

On the infrastructure side, prompt versioning is planned so that the template can evolve as new data fields become available without affecting the content or validity of already-stored reports. Frontend integration of the report view per game page, including a generation trigger visible to admin users and a formatted display component for all users, remains to be implemented.

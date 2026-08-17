## Game Report Generation

The platform includes an automated AI-powered game report feature. After each NFL game, the system generates a written summary that covers the most relevant events, standout performances, and key statistical highlights, without requiring any manual input. The reports are generated through a dedicated backend service that retrieves game data from the database, builds a structured prompt, and calls the Anthropic Claude API to produce the final text.

The process begins when a report is requested for a specific game. The service collects all relevant data from the database: the final score, home and away team information including conference and division, individual player statistics split by role (passing, rushing, receiving, and defensive), and team-level game statistics such as total yards, turnovers, third-down efficiency, penalties, and sacks. This data is then assembled into a natural language prompt that instructs the model to produce a concise and informative match report written from the perspective of a sports analyst.

The current implementation covers the core statistical picture of a game. However, the richness of the reports is directly tied to the data available, and several data sources are still planned for import. As the dataset expands, the quality and depth of the generated reports will improve significantly. The sections below describe both the current state and the planned enhancements.

### Current Report Content

The model is currently provided with tinstead of calling the endpoint game by game, need to create one that processes an entire week at once, with error handling per game and a small delay between calls to avoid hitting API's rate limits. Also, the new data expansion should be included in the report (officials, new game attributes added, draft picks, injuries, snap counts, etc)he following context per game:

- Final score and game metadata (season, week, stadium, surface, roof type, temperature, wind)
- Home and away team names, conference, and division
- Player statistics per individual: passing attempts and completions, passing yards and touchdowns, interceptions, rushing attempts and yards, rushing touchdowns, targets, receptions, receiving yards and touchdowns, tackles, sacks, and forced fumbles
- Team statistics per side: total offensive yards broken down into passing and rushing, turnovers, third-down conversion rate, sacks conceded, penalties, and penalty yards

From this input the model produces a structured narrative that identifies the key performers, explains the flow of the game through the statistical lens available, and highlights any notable outcomes such as dominant individual performances or significant special teams or defensive contributions.

### Planned Improvements

The current single-game endpoint generates one report at a time. A week-level endpoint is needed that processes all games in a given week in a single call, with per-game error handling so that a failure on one game does not abort the rest. A small configurable delay between calls should be introduced to stay within the Anthropic API rate limits. This endpoint will simplify bulk report generation at the end of each Sunday slate or after a full week of games completes.

As new data sources are imported into the platform the reports will gain considerably more depth:

- Play-by-play data — with per-play EPA and WPA values already modelled, the service will be able to identify the highest-leverage moments of the game, describe the sequence of scoring drives, and quantify momentum shifts. This turns a static box score summary into a play-level narrative
- Officials — knowing which officiating crew worked a game adds context to penalty counts and will allow the report to flag games where officiating had a measurable impact on the outcome
- Draft picks — biographical context for players (college, draft round and pick) can be included when highlighting a standout performance, particularly for rookies or early-career players
- Snap counts — percentage of offensive and defensive snaps played per player adds usage context to raw statistics. A receiver with high targets but low snap count tells a different story than one who played every snap
- Injuries — pre-game and in-game injury information will allow the report to contextualise absences, explain unexpected lineup choices, and flag whether a key player's absence was a decisive factor

These additions are already reflected in the data models and import service and will be progressively enabled in the prompt builder as their respective import pipelines are completed.

### What Still Needs to Be Implemented

Beyond the week-level batch endpoint and the data expansions above, the following is still pending on the report generation side:

- Prompt versioning so that as new data fields become available the prompt template can be updated without breaking existing stored reports
- Persistence of the generated report text in the database linked to the game record, avoiding redundant API calls for the same game
- Exposure of the stored report through a dedicated REST endpoint so the frontend can fetch and display it directly
- Frontend integration with a dedicated report view per game page, including a generation trigger for admin users and a display component for all users once the report exists

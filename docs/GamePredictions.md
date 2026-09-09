## Game Predictions

The platform includes a machine learning-inspired prediction system that estimates the win probability of each team before a game is played. Rather than relying on a trained statistical model requiring a separate training pipeline, the system implements an Elo rating methodology entirely in Java, computed directly from the historical game results already stored in the database. This approach is widely used in sports analytics, most notably by FiveThirtyEight's NFL forecasting model, and provides a transparent, auditable alternative to a black-box model while still producing calibrated probabilities.

Each team is assigned a rating that increases or decreases after every completed game, based on whether the result was expected or surprising given the two teams' ratings at the time. A higher-rated team beating a lower-rated team moves both ratings only slightly, while an upset produces a larger swing. Home field advantage is incorporated as a fixed bonus added to the home team's effective rating before computing the expected outcome, and the magnitude of the rating adjustment scales with the margin of victory rather than only the binary win or loss.

### How It Works

The rating update for a single game follows four steps. First, the expected probability of a home win is calculated from the difference between the two teams' current ratings plus the home field advantage, using the standard Elo logistic formula. Second, the actual result is expressed numerically, with a home win equal to 1.0, a home loss equal to 0.0, and a tie equal to 0.5. Third, a margin-of-victory multiplier is applied, based on the score differential and adjusted so that blowout wins against much weaker opponents are not overweighted. Fourth, both teams' ratings are adjusted in opposite directions by the product of a fixed K-factor, the margin multiplier, and the difference between the actual and expected result.

At the start of each new season, all team ratings are partially regressed toward the league mean before the first game of that season is processed. This reflects the reality that rosters, coaching staffs, and team strength change meaningfully during the offseason, and prevents a rating built up over a strong season from carrying forward at full strength into the next one.

### Rating Computation

Ratings are not stored as a single current value per team. Instead, every team receives a new rating snapshot after each game it plays, linked to that specific game, season, and week. This historical record is what allows the system to predict a game using only the information that was actually available at that point in time, without leaking future results into a prediction for an earlier week.

The full recomputation process iterates through every completed game in the database in strict chronological order — sorted by season and then by week — maintaining a running rating for each team in memory and persisting a snapshot after every game. This process is destructive and complete: triggering it deletes all previously stored rating snapshots and rebuilds them from scratch. This is intentional, since it guarantees consistency whenever new games are imported or the rating parameters are adjusted, rather than requiring a more complex incremental update path.

### Generating Predictions

To predict a specific game, the system retrieves the most recent rating snapshot for each team that was recorded strictly before the week being predicted, within the same season. If no such snapshot exists — for example, for a team's first game of a season, before any regression-adjusted rating has been computed — the system falls back to the fixed base rating of 1500. The resulting home win probability, along with both teams' ratings at the time of prediction, is persisted so that the same game does not need to be recalculated on every request, and so that the prediction reflects the information available at prediction time rather than being silently updated as more games are played.

Once a game's actual result is known, the prediction can be evaluated against the outcome. The stored prediction is marked correct or incorrect by comparing the predicted favorite against the actual winner, which enables tracking the model's accuracy over time as more games are evaluated.

### Endpoints

Recomputing all team ratings from the full game history is done with POST /api/admin/ratings/recompute, which requires admin authentication and should be triggered after importing new games. A prediction for a specific game can be retrieved with GET /api/games/{gameId}/prediction; if no prediction exists yet, one is generated on demand using the latest available ratings. Marking a prediction as correct or incorrect after the game has concluded is done with POST /api/admin/games/{gameId}/evaluate-prediction, also protected by admin authentication.

### What Still Needs to Be Implemented

The current implementation relies solely on final score history. Incorporating play-by-play derived metrics such as EPA per play would allow the rating to reflect performance quality rather than only game outcomes, which is particularly valuable for distinguishing a team that won narrowly through variance from one that dominated underlying play. Adjusting a team's effective rating when a starting quarterback or other key starter is ruled out, using the existing injury and depth chart logic already built for game reports, would also improve predictions for games affected by significant absences.

A batch evaluation endpoint that scores every prediction for a full season or week at once, rather than one game at a time, is needed to make tracking model accuracy practical at scale. A dedicated accuracy dashboard — win rate against the model's own confidence bands, performance broken down by season or by team, and comparison against a naive baseline such as always picking the home team — would turn the raw evaluated predictions into something actionable rather than a flat correct/incorrect flag per game.

On the modeling side, the current parameters — the K-factor, home field advantage bonus, and regression-to-mean fraction — are fixed constants chosen from commonly cited NFL Elo implementations rather than fitted against this platform's own historical accuracy. Backtesting these parameters against the imported game history would allow them to be tuned rather than assumed. A more sophisticated statistical model, such as a logistic regression incorporating multiple features beyond a single rating differential, remains a longer-term option if Elo's accuracy ceiling is reached.

Frontend integration of predictions per game page, including a visual win probability display and a running model accuracy indicator visible to all users, remains to be implemented.
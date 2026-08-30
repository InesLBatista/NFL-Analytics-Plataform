package nflanalytics.dto;

//summary of prediction accuracy over a set of evaluated games
//returned by the accuracy endpoint so the caller can track model performance over a season or week
public record PredictionAccuracyResult(
        int season,
        Integer week,       //null when the summary covers the full season
        int totalEvaluated,
        int correct,
        int incorrect,
        double accuracyPct
) {}

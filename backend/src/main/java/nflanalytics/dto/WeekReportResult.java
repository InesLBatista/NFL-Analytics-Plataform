package nflanalytics.dto;

import java.util.List;

//summary returned after generating reports for an entire week
//separates successful generations from failures so the caller knows exactly what happened per game
public record WeekReportResult(
        int season,
        int week,
        int generated,
        int failed,
        int skipped,   //games that already had a report and were not re-generated
        List<String> errors
) {}

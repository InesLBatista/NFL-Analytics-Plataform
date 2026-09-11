package nflanalytics.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

@Component 
public class SeasonWeekCalculator {
    public int getCurrentWeek(int season) {
        LocalDate seasonStart = getApproximateSeasonStart(season);
        LocalDate today = LocalDate.now();

        if (today.isBefore(seasonStart)) return 1;

        long daysSinceStart = ChronoUnit.DAYS.between(seasonStart, today);
        int week = (int) (daysSinceStart / 7) + 1;


        return Math.min(week, 18);
    }

    private LocalDate getApproximateSeasonStart(int season) {
        LocalDate date = LocalDate.of(season, 9, 1);
        while (date.getDayOfWeek().getValue() != 4) { 
            date = date.plusDays(1);
        }
        return date;
    }
}

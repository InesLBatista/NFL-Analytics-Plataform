package nflanalytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nflanalytics.model.Game;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findBySeasonAndWeek(Integer season, Integer week);
    List<Game> findBySeason(Integer season);

    //todos os jogos de uma mesma equipa numa época (home+away)
    @Query("SELECT g FROM Game g WHERE g.season = :season " +
           "AND (g.homeTeam.abbreviation = :abbr OR g.awayTeam.abbreviation = :abbr) " +
           "ORDER BY g.week")
    List<Game> findTeamSeasonGames(@Param("season") Integer season, @Param("abbr") String abbreviation);

    //para encontrar jogo de época/semana em que 2 equipas jogaram entre si 
    //importante já que é necessário ligar game_id do CSV a player_stats do Game.id
    @Query("SELECT g FROM Game g WHERE g.season = :season AND g.week = :week " +
       "AND ((g.homeTeam.abbreviation = :team1 AND g.awayTeam.abbreviation = :team2) " +
       "OR (g.homeTeam.abbreviation = :team2 AND g.awayTeam.abbreviation = :team1))")
    Game findGameByTeams(@Param("season") Integer season, @Param("week") Integer week, @Param("team1") String team1, @Param("team2") String team2);


    //used to connect Injurie a Game
    @Query("SELECT g FROM Game g WHERE g.season = :season AND g.week = :week " +
       "AND (g.homeTeam.abbreviation = :team OR g.awayTeam.abbreviation = :team)")
    Game findGameByTeamAndWeek(@Param("season") Integer season, @Param("week") Integer week, @Param("team") String team);
}
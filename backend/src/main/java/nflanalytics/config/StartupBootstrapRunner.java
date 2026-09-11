package nflanalytics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import nflanalytics.repository.TeamRepository;
import nflanalytics.service.NflverseImportService;

@Component 
@RequiredArgsConstructor 
public class StartupBootstrapRunner implements CommandLineRunner {
    private final TeamRepository teamRepository;
    private final NflverseImportService importService;

    @Value("${automation.bootstrap-on-startup:true}")
    private boolean bootstrapEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (!bootstrapEnabled) return;

        if (teamRepository.count() == 0) {
            System.out.println("DataBase detected- importing both teams and players");
            importService.importTeams();
            importService.importPlayers();
            System.out.println("Inicial Bootstrap completed");
        }
    }
}


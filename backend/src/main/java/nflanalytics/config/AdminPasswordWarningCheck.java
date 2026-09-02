package nflanalytics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AdminPasswordWarningCheck {
    @Value("${admin.password}")
    private String adminPassword;

    private static final String DEFAULT_PASSWORD = "changeme123";

    @PostConstruct
    public void checkPassword() {
        if (DEFAULT_PASSWORD.equals(adminPassword)) {
            System.out.println("\n" +
                "SECURITY WARNING: the admin password is still the\n" +
                "development default (\"" + DEFAULT_PASSWORD + "\"). \n" +
                "Set the ADMIN_PASSWORD environment variable before\n" +
                "exposing this backend outside your local machine.\n");
        }
    }
}

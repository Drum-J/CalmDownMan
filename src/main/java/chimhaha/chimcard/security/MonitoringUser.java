package chimhaha.chimcard.security;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties("custom.monitoring")
public class MonitoringUser {

    private final String username;
    private final String password;
    private final String role;

    public MonitoringUser(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}

package chimhaha.chimcard.jwt;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties("jwt")
public class JwtProperties {
    private final String secret;
    private final String issuer;
    private final long expiration;
    private final long refreshExpiration;

    public JwtProperties(String secret, String issuer, long expiration, long refreshExpiration) {
        this.secret = secret;
        this.issuer = issuer;
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }
}

package chimhaha.chimcard.jwt;

import chimhaha.chimcard.exception.JwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static chimhaha.chimcard.entity.AccountRole.ADMIN;
import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@EnableConfigurationProperties({JwtProperties.class})
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(UTF_8));
    }

    public List<String> generateToken() {
        Date now = new Date();

        String accessToken = tokenBuilder(now, jwtProperties.getExpiration());//access token 생성
        String refreshToken = tokenBuilder(now, jwtProperties.getRefreshExpiration());//refresh token 생성

        return List.of(accessToken, refreshToken);
    }

    public void validateToken(String token) {
        try {
            Jws<Claims> claims = parseToken(token);

            System.out.println(claims.getPayload());
        } catch (ExpiredJwtException e) {
            throw new JwtException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원하지 않는 JWT 토큰입니다.");
        } catch (MalformedJwtException e) {
            throw new JwtException("JWT 구조가 손상되었거나 잘못된 형식입니다.");
        }catch (SecurityException e) {
            throw new JwtException("JWT 서명 검증에 실패했습니다.", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT 토큰이 잘못 되었습니다.", e);
        } catch (Exception e) {
            throw new JwtException("JWT 예기치 않은 예외 발생", e);
        }
    }


    private String tokenBuilder(Date now, long time) {
        Date expirationTime = new Date(now.getTime() + time);

        return Jwts.builder()
                .claims(getClaims())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expirationTime)
                .signWith(secretKey)
                .compact();
    }

    private Claims getClaims() {
        // TODO: 실제 Account_id, Account_nickname, Account_role 가져오기
        return Jwts.claims()
                .add("id",1)
                .add("nickname","testUser")
                .add("role", ADMIN.name())
                .build();
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}

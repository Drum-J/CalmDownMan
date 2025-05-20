package chimhaha.chimcard.jwt;

import chimhaha.chimcard.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static chimhaha.chimcard.entity.AccountRole.ADMIN;

class JwtProviderTest {

    private String secret = "";

    private long expiration = 1000 * 60; // 1분

    private long refreshExpiration = 1000 * 60 * 5; // 5분

    private String issuer = "(주)금병영";

    private SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

    @Test
    void generateToken() throws Exception {
        //given
        Date now = new Date();

        //when
        String accessToken = tokenBuilder(now, expiration);
        String refreshToken = tokenBuilder(now, refreshExpiration);

        //then
        System.out.println("Access token: " + accessToken);
        System.out.println("Refresh token: " + refreshToken);
    }

    @Test
    void validateToken() {
        // generateToken()에서 생성된 token 값 사용
        String token = "";

        try {
            Jws<Claims> claims = parseToken(token);

            System.out.println(claims.getPayload());
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("지원하지 않는 JWT 토큰입니다.");
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("JWT 구조가 손상되었거나 잘못된 형식입니다.");
        }catch (SecurityException e) {
            throw new InvalidTokenException("JWT 서명 검증에 실패했습니다.", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("JWT 토큰이 잘못 되었습니다.", e);
        } catch (Exception e) {
            throw new InvalidTokenException("JWT 예기치 않은 예외 발생", e);
        }
    }

    private String tokenBuilder(Date now, long time) {
        Date expirationTime = new Date(now.getTime() + time);

        return Jwts.builder()
                .claims(getClaims())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expirationTime)
                .signWith(secretKey)
                .compact();
    }

    private Claims getClaims() {
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
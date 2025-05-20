package chimhaha.chimcard.jwt;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.exception.InvalidTokenException;
import chimhaha.chimcard.user.dto.TokenResponseDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
@EnableConfigurationProperties({JwtProperties.class})
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(UTF_8));
    }

    public TokenResponseDto generateToken(Account account) {
        Date now = new Date();

        String accessToken = tokenBuilder(account, now, jwtProperties.getExpiration());//access token 생성
        String refreshToken = tokenBuilder(account, now, jwtProperties.getRefreshExpiration());//refresh token 생성

        return new TokenResponseDto(accessToken, refreshToken);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = parseToken(token);
            log.info("Claims : {}", claims.getPayload().get("id"));
            return true;
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


    public void setAuthentication(String token) {
        Claims claims = parseToken(token).getPayload();

        Long id = claims.get("id", Long.class);
        String role = claims.get("role", String.class);

        UsernamePasswordAuthenticationToken auth
                = new UsernamePasswordAuthenticationToken(id, null, AuthorityUtils.createAuthorityList(role));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String tokenBuilder(Account account, Date now, long time) {
        Date expirationTime = new Date(now.getTime() + time);

        return Jwts.builder()
                .claims(getClaims(account))
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expirationTime)
                .signWith(secretKey)
                .compact();
    }

    private Claims getClaims(Account account) {
        return Jwts.claims()
                .add("id",account.getId())
                .add("nickname",account.getNickname())
                .add("role", account.getRole())
                .build();
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}

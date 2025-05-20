package chimhaha.chimcard.utils;

import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.springframework.boot.web.server.Cookie.SameSite.STRICT;

public class CookieUtils {

    public static final String REFRESH_TOKEN = "refresh_token";

    public static ResponseCookie create(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                .path("/api/token") // 이 경로로 요청할 때 쿠키 자동 전송
                .httpOnly(true) // JS 접근 불가
                .secure(true) // HTTPS 환경에서만 쿠키 전송
                .sameSite(STRICT.attributeValue()) // 다른 사이트에서 오는 요청(CSRF)에는 쿠키를 전송하지 않음
                .maxAge(Duration.ofDays(7)) // 수명
                .build();
    }

    // 위와 똑같은 설정에서 cookie value와 maxAge 만 수정
    public static ResponseCookie delete() {
        return ResponseCookie.from(REFRESH_TOKEN, "") // 빈 값
                .path("/api/token")
                .httpOnly(true)
                .secure(true)
                .sameSite(STRICT.attributeValue())
                .maxAge(0) // 수명 0초
                .build();
    }
}

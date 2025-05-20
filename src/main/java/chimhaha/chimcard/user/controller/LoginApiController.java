package chimhaha.chimcard.user.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.user.dto.LoginRequestDto;
import chimhaha.chimcard.user.dto.TokenResponseDto;
import chimhaha.chimcard.user.service.LoginService;
import chimhaha.chimcard.utils.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginApiController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequestDto dto, HttpServletResponse response) {
        TokenResponseDto token = loginService.login(dto);

        ResponseCookie cookie = CookieUtils.create(token.refreshToken());
        response.setHeader(SET_COOKIE, cookie.toString());

        return ApiResponse.success(token.accessToken());
    }

    @PostMapping("/token/logout")
    public ApiResponse<String> logout(@CookieValue(CookieUtils.REFRESH_TOKEN) String refreshToken, HttpServletResponse response) {
        loginService.logout(refreshToken);
        SecurityContextHolder.clearContext();

        ResponseCookie cookie = CookieUtils.delete();
        response.setHeader(SET_COOKIE, cookie.toString());

        return ApiResponse.success("로그아웃이 완료되었습니다.");
    }

    @PostMapping("/token/refresh")
    public ApiResponse<String> refreshToken(@CookieValue(CookieUtils.REFRESH_TOKEN) String refreshToken, HttpServletResponse response) {
        TokenResponseDto token = loginService.refreshToken(refreshToken);

        ResponseCookie cookie = CookieUtils.create(token.refreshToken());
        response.setHeader(SET_COOKIE, cookie.toString());

        return ApiResponse.success(token.accessToken());
    }
}

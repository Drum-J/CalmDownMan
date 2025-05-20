package chimhaha.chimcard.user.service;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.jwt.JwtProvider;
import chimhaha.chimcard.user.dto.LoginRequestDto;
import chimhaha.chimcard.user.dto.TokenResponseDto;
import chimhaha.chimcard.user.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoginService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public TokenResponseDto login(LoginRequestDto dto) {
        Account account = accountRepository.findByUsername(dto.username())
                .orElseThrow(() -> new ResourceNotFoundException("아이디나 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(dto.password(), account.getPassword())) {
            throw new ResourceNotFoundException("아이디나 비밀번호가 틀렸습니다.");
        }

        TokenResponseDto token = jwtProvider.generateToken(account);
        account.updateToken(token.refreshToken());

        return token;
    }

    public void logout(String refreshToken) {
        Claims claims = jwtProvider.validateToken(refreshToken);
        Long accountId = claims.get("id", Long.class);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        account.updateToken(null);
    }

    public TokenResponseDto refreshToken(String refreshToken) {
        Claims claims = jwtProvider.validateToken(refreshToken);
        Long accountId = claims.get("id", Long.class);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        TokenResponseDto token = jwtProvider.generateToken(account);
        account.updateToken(token.refreshToken());

        return token;
    }
}

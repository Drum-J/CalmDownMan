package chimhaha.chimcard.user.service;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.user.dto.SignUpDto;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static chimhaha.chimcard.common.MessageConstants.*;
import static chimhaha.chimcard.entity.AccountRole.USER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignUpService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpDto dto) {
        checkUsername(dto.getUsername());

        Account account = Account.builder()
                .username(dto.getUsername())
                .nickname(dto.getNickname())
                .password(passwordEncoder.encode(dto.getPassword()))
                .point(1000)
                .role(USER)
                .build();

        accountRepository.save(account);
    }

    public void checkUsername(String username) {
        if (accountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(EXIST_USERNAME);
        }
    }
}


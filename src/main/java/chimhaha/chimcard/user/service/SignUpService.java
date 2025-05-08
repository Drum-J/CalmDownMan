package chimhaha.chimcard.user.service;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.user.dto.SignUpDto;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static chimhaha.chimcard.entity.AccountRole.USER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignUpService {

    private final AccountRepository accountRepository;

    @Transactional
    public void signUp(SignUpDto dto) {
        Account account = Account.builder()
                .username(dto.getUsername())
                .nickname(dto.getNickname())
                .password(dto.getPassword()) // TODO: Spring Security 추가 후 PasswordEncoder 사용
                .role(USER)
                .build();

        accountRepository.save(account);
    }

    public boolean checkUsername(String username) {
        return accountRepository.existsByUsername(username);
    }
}


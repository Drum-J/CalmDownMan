package chimhaha.chimcard.valid;

import chimhaha.chimcard.user.repository.AccountRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueNicknameValidator implements ConstraintValidator<UniqueNickname, String> {

    private final AccountRepository accountRepository;

    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext context) {
        return !accountRepository.existsByNickname(nickname); //중복 닉네임이 없으면 true 반환
    }
}

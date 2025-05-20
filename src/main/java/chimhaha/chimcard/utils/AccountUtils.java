package chimhaha.chimcard.utils;

import org.springframework.security.core.context.SecurityContextHolder;

public class AccountUtils {
    public static Long getAccountId() {
        Long id = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (id == null) {
            throw new IllegalArgumentException("로그인 정보를 찾을 수 없습니다.");
        }

        return id;
    }
}

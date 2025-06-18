package chimhaha.chimcard.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import static chimhaha.chimcard.common.MessageConstants.*;

public class AccountUtils {
    public static Long getAccountId() {
        Long id = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (id == null) {
            throw new IllegalArgumentException(LOGIN_INFO_NOT_FOUND);
        }

        return id;
    }
}

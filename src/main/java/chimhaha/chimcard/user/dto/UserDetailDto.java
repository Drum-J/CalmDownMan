package chimhaha.chimcard.user.dto;

import chimhaha.chimcard.entity.Account;

public record UserDetailDto(Long id, String username, String nickname) {
    public UserDetailDto(Account account) {
        this(account.getId(), account.getUsername(), account.getNickname());
    }
}

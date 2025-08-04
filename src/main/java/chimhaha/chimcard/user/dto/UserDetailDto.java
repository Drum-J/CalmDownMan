package chimhaha.chimcard.user.dto;

import chimhaha.chimcard.entity.Account;

public record UserDetailDto(Long id, String username, String nickname,
                            String profileImage, Integer point, Integer rankScore,
                            int winCount, int loseCount, int drawCount, String role
) {
    public UserDetailDto(Account account) {
        this(account.getId(), account.getUsername(), account.getNickname(),
                account.getProfileImage(), account.getPoint(), account.getRankScore(),
                account.getWin(), account.getLose(), account.getDraw(), account.getRole().name()
        );
    }
}

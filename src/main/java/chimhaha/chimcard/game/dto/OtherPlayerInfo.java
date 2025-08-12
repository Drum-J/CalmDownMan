package chimhaha.chimcard.game.dto;

import chimhaha.chimcard.entity.Account;

public record OtherPlayerInfo(String nickname, String imageUrl, int rankScore, int win, int draw, int lose) {

    public OtherPlayerInfo(Account account) {
        this(account.getNickname(), account.getProfileImage(),
                account.getRankScore(), account.getWin(), account.getDraw(), account.getLose());
    }
}

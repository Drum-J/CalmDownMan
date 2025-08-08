package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity @Getter
@NoArgsConstructor(access = PROTECTED)
@Table(
    indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_nickname", columnList = "nickname"),
    }
)
public class Account extends TimeStamped {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private Integer point;
    private Integer rankScore;
    private String profileImage;
    @Column(length = 500)
    private String refreshToken;

    @Enumerated(value = STRING)
    private AccountRole role;

    private int win;
    private int lose;
    private int draw;

    @Version
    private Long version;

    @Builder
    public Account(Long id, String username, String password, String nickname,Integer point, AccountRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.point = point;
        this.rankScore = 0;
        this.role = role;
        this.profileImage = null;
        this.refreshToken = null;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updateRole(AccountRole role) {
        this.role = role;
    }

    public void updateToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void increasePoint(int plus) {
        point += plus;
    }

    public void decreasePoint(int minus) {
        if (point < minus) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다.");
        }

        point -= minus;
    }

    public void updateRankScore(int rankScore) {
        this.rankScore += rankScore;
    }

    public boolean equals(Long accountId) {
        return Objects.equals(id, accountId);
    }

    public void win() {
        win++;
    }

    public void lose() {
        lose++;
    }

    public void draw() {
        draw++;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Account account)) return false;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", role=" + role.name() +
                '}';
    }
}

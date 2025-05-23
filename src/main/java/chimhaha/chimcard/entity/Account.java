package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String profileImage;
    private String refreshToken;

    @Enumerated(value = STRING)
    private AccountRole role;

    @Builder
    public Account(String username, String password, String nickname, AccountRole role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
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

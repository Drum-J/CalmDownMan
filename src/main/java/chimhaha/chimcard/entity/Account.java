package chimhaha.chimcard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity @Getter
@NoArgsConstructor(access = PROTECTED)
public class Account extends TimeStamped {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String profileImage;

    @Enumerated(value = STRING)
    private AccountRole role;

    @Builder
    public Account(String username, String password, String nickname, AccountRole role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.profileImage = null;
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
}

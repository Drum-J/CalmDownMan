package chimhaha.chimcard.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpDto {

    @NotBlank(message = "username")
    @Size(min = 4, message = "username")
    private String username;

    @NotBlank(message = "nickname")
    @Size(min = 2, message = "nickname")
    private String nickname;

    @NotBlank(message = "password")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "password"
    )
    private String password;

    @Builder
    public SignUpDto(String username, String nickname, String password) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
    }
}

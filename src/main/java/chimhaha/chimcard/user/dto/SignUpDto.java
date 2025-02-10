package chimhaha.chimcard.user.dto;

import chimhaha.chimcard.valid.UniqueNickname;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpDto {

    @NotBlank(message = "아이디를 입력해 주세요.")
    @Size(min = 4, message = "아이디는 4글자 이상으로 입력해 주세요.")
    private String username;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, message = "닉네임은 2글자 이상으로 입력해 주세요.")
    @UniqueNickname
    private String nickname;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자 포함 8~20글자 이하로 입력해 주세요."
    )
    private String password;

    @Builder
    public SignUpDto(String username, String nickname, String password) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
    }
}

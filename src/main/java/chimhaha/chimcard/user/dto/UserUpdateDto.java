package chimhaha.chimcard.user.dto;

import org.springframework.web.multipart.MultipartFile;

public record UserUpdateDto(String nickname, String password, MultipartFile profileImage) {
}

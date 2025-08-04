package chimhaha.chimcard.common;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidator {
    // 허용할 이미지 확장자 목록
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

    // 허용할 이미지 MIME 타입 목록
    private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/gif");

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없습니다.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("파일 이름을 확인해주세요.");
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("확장자를 확인해 주세요. [jpg,jpeg,png,gif] 파일만 가능합니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedMimeType(contentType)) {
            throw new IllegalArgumentException("파일 타입(MIME)을 확인해주세요.");
        }
    }

    private static boolean isAllowedMimeType(String contentType) {
        return  ALLOWED_MIME_TYPES.stream().anyMatch(contentType::startsWith);
    }
}

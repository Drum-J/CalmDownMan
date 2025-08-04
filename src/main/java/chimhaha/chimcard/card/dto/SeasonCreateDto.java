package chimhaha.chimcard.card.dto;

import org.springframework.web.multipart.MultipartFile;

public record SeasonCreateDto(String seasonName, MultipartFile seasonImage) {
}

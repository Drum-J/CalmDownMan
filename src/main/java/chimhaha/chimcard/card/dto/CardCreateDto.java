package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Grade;
import org.springframework.web.multipart.MultipartFile;

public record CardCreateDto(String title, int power, AttackType attackType, Grade grade,
                            Long cardSeasonId, MultipartFile cardImage) {
}

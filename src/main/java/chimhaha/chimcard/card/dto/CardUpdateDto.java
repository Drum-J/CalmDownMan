package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Grade;

public record CardUpdateDto(Long id, String title, int power, AttackType attackType, Grade grade) {
}

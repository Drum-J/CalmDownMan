package chimhaha.chimcard.game.dto;

import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.Grade;

public record MyGameCardDto(Long gameCardId, Long cardId, String title, String attackType, Grade grade, int power, String imageUrl) {
    public MyGameCardDto(Long gameCardId, Card card) {
        this(gameCardId, card.getId(), card.getTitle(), card.getAttackType().getType(), card.getGrade(), card.getPower(), card.getImageUrl());
    }
}

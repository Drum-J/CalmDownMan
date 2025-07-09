package chimhaha.chimcard.game.dto;

import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.Grade;

public record MyGameCardDto(Long id, String title, AttackType attackType, Grade grade, int power, String imageUrl) {
    public MyGameCardDto(Card card) {
        this(card.getId(), card.getTitle(), card.getAttackType(), card.getGrade(), card.getPower(), card.getImageUrl());
    }
}

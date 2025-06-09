package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.Grade;
import lombok.Getter;

@Getter
public class CardResponseDto {
    private final Long id;
    private final String title;
    private final String attackType;
    private final Grade grade;
    private final int power;
    private final String imageUrl;
    private final String cardSeason;

    public CardResponseDto(Card card) {
        this.id = card.getId();
        this.title = card.getTitle();
        this.attackType = card.getAttackType().getType();
        this.grade = card.getGrade();
        this.power = card.getPower();
        this.imageUrl = card.getImageUrl();
        this.cardSeason = card.getCardSeason().getSeasonName();
    }
}

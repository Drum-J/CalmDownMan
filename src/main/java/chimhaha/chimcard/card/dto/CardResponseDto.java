package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.Grade;
import lombok.Getter;

@Getter
public class CardResponseDto {
    private final String title;
    private final String attackType;
    private final Grade grade;
    private final int power;
    private final String cardSeason;

    public CardResponseDto(Card card) {
        this.title = card.getTitle();
        this.attackType = card.getAttackType().getType();
        this.grade = card.getGrade();
        this.power = card.getPower();
        this.cardSeason = card.getCardSeason().getSeasonName();
    }
}

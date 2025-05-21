package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.Grade;
import lombok.Getter;

@Getter
public class MyCardResponseDto {
    private final Long id;
    private final String title;
    private final String attackType;
    private final Grade grade;
    private final int power;
    private final String cardSeason;
    private final int count;

    public MyCardResponseDto(Card card, int count) {
        this.id = card.getId();
        this.title = card.getTitle();
        this.attackType = card.getAttackType().getType();
        this.grade = card.getGrade();
        this.power = card.getPower();
        this.cardSeason = card.getCardSeason().getSeasonName();
        this.count = count;
    }
}

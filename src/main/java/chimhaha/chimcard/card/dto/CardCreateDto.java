package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.Grade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateDto {
    private String title;
    private int power;
    private AttackType attackType;
    private Grade grade;

    public Card toEntity() {
        return Card.builder()
                .title(this.title)
                .power(this.power)
                .attackType(this.attackType)
                .grade(this.grade)
                .build();
    }
}

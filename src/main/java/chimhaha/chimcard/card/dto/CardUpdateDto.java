package chimhaha.chimcard.card.dto;

import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Grade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardUpdateDto {
    private Long id;
    private String title;
    private int power;
    private AttackType attackType;
    private Grade grade;
}

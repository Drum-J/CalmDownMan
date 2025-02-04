package chimhaha.chimcard.admin.dto;

import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Grade;
import lombok.Getter;

@Getter
public class CardCreateDto {
    private String title;
    private int power;
    private AttackType attackType;
    private Grade grade;
    private Long cardSeasonId;
}

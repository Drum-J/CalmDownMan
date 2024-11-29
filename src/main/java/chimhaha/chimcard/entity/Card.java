package chimhaha.chimcard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity @Getter
@NoArgsConstructor(access = PROTECTED)
public class Card {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String title;

    @Enumerated(value = STRING)
    private AttackType attackType;

    @Enumerated(value = STRING)
    private Grade grade;

    private int power;

    @Builder
    public Card(String title, AttackType attackType, Grade grade, int power) {
        this.title = title;
        this.attackType = attackType;
        this.grade = grade;
        this.power = power;
    }

    public void update(String title, int power, AttackType attackType, Grade grade) {
        this.title = title;
        this.attackType = attackType;
        this.grade = grade;
        this.power = power;
    }
}

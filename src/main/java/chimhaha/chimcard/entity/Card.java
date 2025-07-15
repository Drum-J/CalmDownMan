package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity @Getter
@NoArgsConstructor(access = PROTECTED)
public class Card extends TimeStamped {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String title;

    @Enumerated(value = STRING)
    private AttackType attackType;

    @Enumerated(value = STRING)
    private Grade grade;

    private int power;

    @Column(length = 500)
    private String imageUrl;

    @ManyToOne(fetch = LAZY)
    private CardSeason cardSeason;

    @Builder
    public Card(Long id, String title, AttackType attackType, Grade grade, int power,String imageUrl, CardSeason cardSeason) {
        this.id = id;
        this.title = title;
        this.attackType = attackType;
        this.grade = grade;
        this.power = power;
        this.imageUrl = imageUrl;
        this.cardSeason = cardSeason;
    }

    public void update(String title, int power, AttackType attackType, Grade grade) {
        this.title = title;
        this.attackType = attackType;
        this.grade = grade;
        this.power = power;
    }

    // 승: 1 무승부: 0 패: -1
    public int match(Card other) {
        int typeResult = matchType(this, other);
        if (typeResult != 0) { // type에서 승패 결정 시 즉시 반환
            return typeResult;
        }

        int powerResult = matchPower(this, other);
        if (powerResult != 0) { // power에서 승패 결정 시 즉시 반환
            return powerResult;
        }

        return matchGrade(this, other); // type,power 모두 무승부일 시 grade 결과 반환
    }

    // 승: 1 무승부: 0 패: -1
    private int matchType(Card card, Card other) {
        return card.getAttackType().compare(other.getAttackType());
    }

    private int matchPower(Card card, Card other) {
        return Integer.compare(card.power, other.power);
    }

    private int matchGrade(Card card, Card other) {
        return Integer.compare(card.grade.getValue(), other.grade.getValue()) * -1; // Grade.value 는 등급이 높을수록 숫자가 낮기 때문에 -1 곱해줌
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Card card)) return false;
        return Objects.equals(id, card.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Card{" +
                title +
                ", " + grade +
                ", " + attackType +
                ", " + power +
                "}";
    }
}

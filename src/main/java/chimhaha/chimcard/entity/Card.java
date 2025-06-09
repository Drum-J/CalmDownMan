package chimhaha.chimcard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

    private String imageUrl;

    @ManyToOne(fetch = LAZY)
    private CardSeason cardSeason;

    @Builder
    public Card(String title, AttackType attackType, Grade grade, int power,String imageUrl, CardSeason cardSeason) {
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

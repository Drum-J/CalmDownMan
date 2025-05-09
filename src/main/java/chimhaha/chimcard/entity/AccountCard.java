package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(
    indexes = {
        @Index(name = "idx_account_id", columnList = "account_id"),
        @Index(name = "idx_card_id", columnList = "card_id"),
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_card", columnNames = {"account_id", "card_id"})
    }
)
public class AccountCard extends TimeStamped{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    private int count; // 카드 갯수

    public AccountCard(Account account, Card card) {
        this.account = account;
        this.card = card;
        this.count = 1;
    }

    public void increaseCount() {
        this.count++;
    }
}

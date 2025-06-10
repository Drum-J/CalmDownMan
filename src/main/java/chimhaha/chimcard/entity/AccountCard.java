package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

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

    private long count; // 카드 갯수

    public AccountCard(Account account, Card card) {
        this(account, card, 1);
    }

    public AccountCard(Account account, Card card, long count) {
        this.account = account;
        this.card = card;
        this.count = count;
    }

    /** Deprecated: test code와 운영 로직의 변경으로 인해 추후 삭제 예정 */
    public void increaseCount() {
        this.count++;
    }

    public void increaseCount(long count) {
        this.count += count;
    }

    /** Deprecated: test code와 운영 로직의 변경으로 인해 추후 삭제 예정 */
    public boolean decreaseCount() {
        this.count--;
        return count == 0;
    }

    public boolean decreaseCount(long minus) {
        if (count < minus) {
            throw new IllegalArgumentException("카드 수량이 부족합니다. 현재: " + count + ", 요청: " + minus);
        }

        count -= minus;
        return count == 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AccountCard that)) return false;
        return Objects.equals(account, that.account) && Objects.equals(card, that.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, card);
    }
}

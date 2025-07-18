package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static chimhaha.chimcard.common.MessageConstants.IS_NOT_WAITING;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

/**
 * 교환글 작성자 엔티티
 */

@Entity @Getter
@NoArgsConstructor(access = PROTECTED)
public class TradePost extends TimeStamped {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account owner;

    @OneToMany(mappedBy = "tradePost", cascade = ALL, orphanRemoval = true)
    private final List<TradePostCard> ownerCards = new ArrayList<>();

    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    public TradePost(Account owner, String title, String content) {
        this.owner = owner;
        this.title = title;
        this.content = content;
        this.tradeStatus = TradeStatus.WAITING;
        this.grade = null;
    }

    public void topGrade(Grade grade) {
        this.grade = grade;
    }

    public void addCard(TradePostCard card) {
        ownerCards.add(card);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void complete() {
        this.tradeStatus = TradeStatus.COMPLETED;
    }

    public void cancel() {
        this.tradeStatus = TradeStatus.CANCEL;
    }

    public void isWaiting() {
        if (tradeStatus != TradeStatus.WAITING) {
            throw new IllegalArgumentException(IS_NOT_WAITING);
        }
    }

    public boolean isOwner(Long ownerId) {
        return owner.equals(ownerId);
    }
}

package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    public TradePost(Account owner) {
        this.owner = owner;
        this.tradeStatus = TradeStatus.WAITING;
    }

    public void complete() {
        this.tradeStatus = TradeStatus.COMPLETED;
    }

    public void cancel() {
        this.tradeStatus = TradeStatus.CANCEL;
    }
}

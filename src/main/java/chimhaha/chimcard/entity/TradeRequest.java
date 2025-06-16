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
 * 교환 신청자 엔티티
 */

@Entity @Getter
@NoArgsConstructor(access = PROTECTED)
public class TradeRequest extends TimeStamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private TradePost tradePost; //어느 교환글에 신청했는가

    @ManyToOne(fetch = LAZY)
    private Account requester;

    @OneToMany(mappedBy = "tradeRequest", cascade = ALL, orphanRemoval = true)
    private final List<TradeRequestCard> requesterCards = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    public TradeRequest(TradePost tradePost, Account requester) {
        this.tradePost = tradePost;
        this.requester = requester;
        this.tradeStatus = TradeStatus.WAITING;
    }

    public void addCard(TradeRequestCard card) {
        requesterCards.add(card);
    }

    public void complete() {
        this.tradeStatus = TradeStatus.COMPLETED;
    }

    public void cancel() {
        this.tradeStatus = TradeStatus.CANCEL;
    }

    public void reject(){
        this.tradeStatus = TradeStatus.REJECTED;
    }

    public boolean isWaiting() {
        return tradeStatus == TradeStatus.WAITING;
    }
}

package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

/**
 * 교환 신성자가 제시한 카드 엔티티
 */

@Entity @Getter
@NoArgsConstructor(access = PROTECTED)
public class TradeRequestCard extends TimeStamped {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private TradeRequest tradeRequest;

    @ManyToOne(fetch = LAZY)
    private Card card;

    private long count;

    public TradeRequestCard(TradeRequest tradeRequest, Card card, long count) {
        this.tradeRequest = tradeRequest;
        this.card = card;
        this.count = count;

        tradeRequest.addCard(this);
    }
}

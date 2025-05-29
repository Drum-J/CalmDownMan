package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

/**
 * 교환글 작성자가 제시한 카드 엔티티
 */

@Entity @Getter
@NoArgsConstructor(access = PROTECTED)
public class TradePostCard extends TimeStamped {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private TradePost tradePost;

    @ManyToOne(fetch = LAZY)
    private Card card;

    private long count;

    public TradePostCard(TradePost tradePost, Card card, long count) {
        this.tradePost = tradePost;
        this.card = card;
        this.count = count;
    }
}

package chimhaha.chimcard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class FailedTrade extends TimeStamped {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private TradeRequest tradeRequest;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    public FailedTrade(TradeRequest tradeRequest, TradeStatus tradeStatus) {
        this.tradeRequest = tradeRequest;
        this.tradeStatus = tradeStatus;
    }
}

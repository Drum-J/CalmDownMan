package chimhaha.chimcard.trade.repository;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.TradePost;
import chimhaha.chimcard.entity.TradeRequest;
import chimhaha.chimcard.entity.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {
    List<TradeRequest> findByTradePostAndTradeStatus(TradePost tradePost, TradeStatus tradeStatus);

    List<TradeRequest> findByRequester(Account requester); // Test Code 용
}

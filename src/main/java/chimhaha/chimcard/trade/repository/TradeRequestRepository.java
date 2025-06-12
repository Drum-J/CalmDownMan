package chimhaha.chimcard.trade.repository;

import chimhaha.chimcard.entity.TradePost;
import chimhaha.chimcard.entity.TradeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {
    List<TradeRequest> findByTradePost(TradePost tradePost);
}

package chimhaha.chimcard.trade.repository;

import chimhaha.chimcard.entity.FailedTrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedTradeRepository extends JpaRepository<FailedTrade, Long> {
}

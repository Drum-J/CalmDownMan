package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.entity.TradeRequest;
import chimhaha.chimcard.entity.TradeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeAsyncService {

    private final TradeAsyncTrxService tradeAsyncTrxService;

    @Async
    public void asyncTrade(List<TradeRequest> allRequests, TradeStatus status) {
        for (TradeRequest request : allRequests) {
            try {
                tradeAsyncTrxService.rollbackRequestCard(request, status); // 신청자 카드 돌려주기
            } catch (Exception e) {
                log.error("asyncTrade error : {}", e.getMessage());
                tradeAsyncTrxService.saveFailedRequest(request, status);
            }
        }
    }
}

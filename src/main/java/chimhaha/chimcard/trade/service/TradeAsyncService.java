package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.entity.TradeRequest;
import chimhaha.chimcard.trade.event.TradeCompleteOrCancelEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeAsyncService {

    private final TradeAsyncTrxService tradeAsyncTrxService;
    private static final int MAX_RETRIES = 3; // 최대 재시도 횟수

    @Async
    @TransactionalEventListener
    public void asyncTrade(TradeCompleteOrCancelEvent event) {
        for (TradeRequest request : event.requests()) {
            boolean success = false;
            int retries = 0;
            while (!success && retries < MAX_RETRIES) {
                try {
                    tradeAsyncTrxService.rollbackRequestCard(request.getId(), event.status()); // 신청자 카드 돌려주기
                    success = true;
                } catch (OptimisticLockingFailureException e) {
                    retries++;
                    log.warn("카드 롤백 중 낙관적 락 예외 발생. 재시도합니다. (요청 ID: {}, 시도 횟수: {})", request.getId(), retries);
                    try {
                        Thread.sleep(100); // 잠시 대기 후 재시도
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (!success) {
                log.error("최대 재시도 횟수({})를 초과하여 카드 롤백에 실패했습니다. (요청 ID: {})", MAX_RETRIES, request.getId());
                tradeAsyncTrxService.saveFailedRequest(request.getId(), event.status());
            }
        }
    }
}
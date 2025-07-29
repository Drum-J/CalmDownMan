package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.FailedTrade;
import chimhaha.chimcard.entity.TradeRequest;
import chimhaha.chimcard.entity.TradeStatus;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.trade.repository.FailedTradeRepository;
import chimhaha.chimcard.trade.repository.TradeRequestRepository;
import io.micrometer.core.annotation.Counted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static chimhaha.chimcard.common.MessageConstants.TRADE_REQUEST_NOT_FOUND;
import static chimhaha.chimcard.utils.CardUtils.getRequestCards;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class TradeAsyncTrxService {

    private final CardService cardService;
    private final FailedTradeRepository failedTradeRepository;
    private final TradeRequestRepository requestRepository;

    @Counted("trade.async")
    public void rollbackRequestCard(Long requestId, TradeStatus status) {
        log.info("rollbackRequestCard Async Thread : [{}] {}, {}",Thread.currentThread().getName(), requestId, status);
        try {
            TradeRequest request = getTradeRequest(requestId);
            switch (status) {
                case REJECTED -> request.reject();
                case CANCEL -> request.cancel();
            }

            Map<Card, Long> requestCardMap = getRequestCards(request);
            cardService.upsertList(request.getRequester(), requestCardMap);
        } catch (IllegalArgumentException e) {
            log.warn("request is not waiting status : [{}] ", requestId);
        }
    }

    @Counted("trade.async")
    public void saveFailedRequest(Long requestId, TradeStatus status) {
        log.info("saveFailedRequest Async Thread : [{}] {}, {}",Thread.currentThread().getName(), requestId, status);
        TradeRequest request = getTradeRequest(requestId);
        FailedTrade failedTrade = new FailedTrade(request, status);

        failedTradeRepository.save(failedTrade);
    }

    private TradeRequest getTradeRequest(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(()-> new ResourceNotFoundException(TRADE_REQUEST_NOT_FOUND));
    }
}

package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.FailedTrade;
import chimhaha.chimcard.entity.TradeRequest;
import chimhaha.chimcard.entity.TradeStatus;
import chimhaha.chimcard.trade.repository.FailedTradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static chimhaha.chimcard.utils.CardUtils.getRequestCards;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class TradeAsyncTrxService {

    private final CardService cardService;
    private final FailedTradeRepository failedTradeRepository;

    public void rollbackRequestCard(TradeRequest request, TradeStatus status) {
        log.info("rollbackRequestCard Async Thread : [{}] {}, {}",Thread.currentThread().getName(), request.getId(), status);
        Map<Card, Long> requestCardMap = getRequestCards(request);
        cardService.upsertList(request.getRequester(), requestCardMap);

        switch (status) {
            case REJECTED -> request.reject();
            case CANCEL -> request.cancel();
        }
    }

    public void saveFailedRequest(TradeRequest request, TradeStatus status) {
        log.info("saveFailedRequest Async Thread : [{}] {}, {}",Thread.currentThread().getName(), request.getId(), status);
        FailedTrade failedTrade = new FailedTrade(request, status);

        failedTradeRepository.save(failedTrade);
    }
}

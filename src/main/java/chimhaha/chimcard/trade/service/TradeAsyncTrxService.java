package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.TradeRequest;
import chimhaha.chimcard.entity.TradeStatus;
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

    public void rollbackRequestCard(TradeRequest request, TradeStatus status) {
        log.info("rollbackRequestCard Async Thread : [{}] {}, {}",Thread.currentThread().getName(), request.getId(), status);
        Map<Card, Long> requestCardMap = getRequestCards(request);
        cardService.upsertList(request.getRequester(), requestCardMap);

        switch (status) {
            case REJECTED -> request.reject();
            case CANCEL -> request.cancel();
        }
    }

    public void saveFailedRequest(TradeRequest request) {
        // TODO: 실패한 데이터 저장 로직 구현 필요
    }
}

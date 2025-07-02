package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.card.repository.CardCustomRepository;
import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.trade.dto.TradePostCreateDto;
import chimhaha.chimcard.trade.dto.TradeRequestCreateDto;
import chimhaha.chimcard.trade.dto.TradeStatusRequestDto;
import chimhaha.chimcard.trade.event.TradeCompleteOrCancelEvent;
import chimhaha.chimcard.trade.repository.TradePostRepository;
import chimhaha.chimcard.trade.repository.TradeRequestRepository;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static chimhaha.chimcard.common.MessageConstants.*;
import static chimhaha.chimcard.entity.TradeStatus.*;
import static chimhaha.chimcard.utils.CardUtils.*;
import static chimhaha.chimcard.common.PointConstants.POINT_OF_TRADE_COMPLETE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final CardCustomRepository cardCustomRepository;
    private final AccountRepository accountRepository;
    private final AccountCardRepository accountCardRepository;
    private final TradePostRepository tradePostRepository;
    private final TradeRequestRepository tradeRequestRepository;
    private final CardService cardService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void tradePost(Long accountId, TradePostCreateDto dto) {
        Account owner = getAccount(accountId);
        Map<Card, Long> cardMap = checkAndUpdateCard(accountId, dto.cardIds());

        TradePost tradePost = new TradePost(owner, dto.title(), dto.content());
        int gradeValue = 6;
        for (Map.Entry<Card, Long> entry : cardMap.entrySet()) {
            Card card = entry.getKey();
            new TradePostCard(tradePost, card, entry.getValue());

            int cardValue = card.getGrade().getValue();
            if (cardValue < gradeValue) {
                gradeValue = cardValue;
            }
        }
        tradePost.topGrade(Grade.getEnum(gradeValue));

        tradePostRepository.save(tradePost);
    }

    @Transactional
    public void tradeRequest(Long postId, Long requesterId, TradeRequestCreateDto dto) {
        TradePost tradePost = getTradePost(postId);
        tradePost.isWaiting();

        if (tradePost.isOwner(requesterId)) {
            throw new IllegalArgumentException("본인의 교환글에는 신청할 수 없습니다.");
        }

        Account requester = getAccount(requesterId);
        Map<Card, Long> cardMap = checkAndUpdateCard(requesterId, dto.cardIds());

        TradeRequest tradeRequest = new TradeRequest(tradePost, requester);
        for (Map.Entry<Card, Long> entry : cardMap.entrySet()) {
            new TradeRequestCard(tradeRequest, entry.getKey(), entry.getValue());
        }

        tradeRequestRepository.save(tradeRequest);
    }

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void tradeComplete(Long postId, Long ownerId, TradeStatusRequestDto dto) {
        // 교환글 조회
        TradePost tradePost = isTradeOwner(postId, ownerId);
        tradePost.isWaiting();

        // 교환 신청 조회
        TradeRequest tradeRequest = isCorrectRequest(postId, dto);

        // 교환 실행
        complete(tradePost, tradeRequest);

        // 교환되지 않은 카드 복구 비동기 처리
        List<TradeRequest> allRequests =
                tradeRequestRepository.findByTradePostAndTradeStatus(tradePost, WAITING)
                        .stream().filter(request -> !request.getId().equals(dto.requestId())).toList();

        if (!allRequests.isEmpty()) {
            eventPublisher.publishEvent(new TradeCompleteOrCancelEvent(allRequests, REJECTED));
        }
    }

    @Transactional
    public void tradeReject(Long postId, Long ownerId, TradeStatusRequestDto dto) {
        isTradeOwner(postId, ownerId);

        TradeRequest tradeRequest = isCorrectRequest(postId, dto);
        Map<Card, Long> cardMap = getRequestCards(tradeRequest);

        cardService.upsertList(tradeRequest.getRequester(), cardMap);
        tradeRequest.reject();
    }

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void postCancel(Long postId, Long accountId) {
        TradePost tradePost = isTradeOwner(postId, accountId);
        tradePost.isWaiting();

        // 본인 카드 복구
        Map<Card, Long> cardMap = getPostCards(tradePost);
        cardService.upsertList(tradePost.getOwner(), cardMap);
        tradePost.cancel();

        // 신청 카드 복구 비동기 처리
        List<TradeRequest> allRequests = tradeRequestRepository.findByTradePostAndTradeStatus(tradePost, WAITING);
        if (!allRequests.isEmpty()) {
            eventPublisher.publishEvent(new TradeCompleteOrCancelEvent(allRequests, CANCEL));
        }
    }

    @Transactional
    public void requestCancel(Long requestId, Long accountId) {
        TradeRequest tradeRequest = getTradeRequest(requestId);

        if (!tradeRequest.getRequester().getId().equals(accountId)) {
            throw new AccessDeniedException(NOT_TRADE_OWNER);
        }

        Map<Card, Long> cardMap = getRequestCards(tradeRequest);
        cardService.upsertList(tradeRequest.getRequester(), cardMap);

        tradeRequest.cancel();
    }

    /***** private method *****/
    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND));
    }

    private TradePost getTradePost(Long postId) {
        return tradePostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(TRADE_POST_NOT_FOUND));
    }

    private TradeRequest getTradeRequest(Long requestId) {
        return tradeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(TRADE_REQUEST_NOT_FOUND));
    }

    private TradePost isTradeOwner(Long postId, Long ownerId) {
        TradePost tradePost = getTradePost(postId);

        if (!tradePost.isOwner(ownerId)) {
            throw new AccessDeniedException(NOT_TRADE_OWNER);
        }

        return tradePost;
    }

    private TradeRequest isCorrectRequest(Long postId, TradeStatusRequestDto dto) {
        TradeRequest tradeRequest = getTradeRequest(dto.requestId());

        if (!tradeRequest.getTradePost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 교환글의 신청이 아닙니다.");
        }

        tradeRequest.isWaiting();

        return tradeRequest;
    }

    /**
     * 교환 등록/신청 전 내 카드 체크 및 갯수 업데이트
     */
    private Map<Card, Long> checkAndUpdateCard(Long accountId, List<Long> cardIds) {
        // 카드 ID - 갯수 Map
        Map<Long, Long> cardCountMap = cardCountMap(cardIds);

        List<AccountCard> accountCards = cardCustomRepository.getMyCardByCardIds(accountId, cardCountMap.keySet());
        // 카드 ID - AccountCard Map
        Map<Long, AccountCard> myCardMap = accountCardMapLong(accountCards);

        Map<Card, Long> cardMap = new HashMap<>();
        for (Map.Entry<Long, Long> entry : cardCountMap.entrySet()) {
            Long cardId = entry.getKey();
            Long count = entry.getValue();

            AccountCard accountCard = myCardMap.get(cardId);
            if (accountCard == null) {
                throw new ResourceNotFoundException("보유하지 않은 카드입니다. cardId=" + cardId);
            }

            if (accountCard.decreaseCount(count)) {
                accountCardRepository.delete(accountCard);
            }

            cardMap.put(accountCard.getCard(), count);
        }

        return cardMap;
    }

    private void complete(TradePost tradePost, TradeRequest tradeRequest) {
        Account owner = tradePost.getOwner();
        Account requester = tradeRequest.getRequester();

        Map<Card, Long> ownerCardMap = getPostCards(tradePost);
        Map<Card, Long> requestCardMap = getRequestCards(tradeRequest);

        try {
            // 카드 저장
            cardService.upsertList(owner, requestCardMap);
            cardService.upsertList(requester, ownerCardMap);

            // 교환 상태 변경
            tradePost.complete();
            tradeRequest.complete();

            // 교환 성공 포인트 지급
            owner.increasePoint(POINT_OF_TRADE_COMPLETE);
            requester.increasePoint(POINT_OF_TRADE_COMPLETE);
        } catch (Exception e) {
            throw new IllegalStateException("카드 교환 중 에러가 발생했습니다.");
        }
    }
}

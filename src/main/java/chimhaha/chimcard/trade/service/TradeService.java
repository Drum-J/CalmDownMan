package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.card.repository.CardCustomRepository;
import chimhaha.chimcard.card.service.CardService;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.trade.dto.TradePostCreateDto;
import chimhaha.chimcard.trade.dto.TradeRequestCreateDto;
import chimhaha.chimcard.trade.dto.TradeStatusRequestDto;
import chimhaha.chimcard.trade.repository.TradePostRepository;
import chimhaha.chimcard.trade.repository.TradeRequestRepository;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static chimhaha.chimcard.utils.CardUtils.accountCardMapLong;
import static chimhaha.chimcard.utils.CardUtils.cardCountMap;

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

    @Transactional
    public void tradePost(Long accountId, TradePostCreateDto dto) {
        Account owner = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        Map<Card, Long> cardMap = checkAndUpdateCard(accountId, dto.cardIds());

        TradePost tradePost = new TradePost(owner, dto.title(), dto.content());
        for (Map.Entry<Card, Long> entry : cardMap.entrySet()) {
            new TradePostCard(tradePost, entry.getKey(), entry.getValue());
        }

        tradePostRepository.save(tradePost);
    }

    @Transactional
    public void tradeRequest(Long postId, Long requesterId, TradeRequestCreateDto dto) {
        TradePost tradePost = tradePostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 교환글을 찾을 수 없습니다."));

        if (!tradePost.isWaiting()) {
            throw new IllegalArgumentException("교환 대기 중인 글에만 신청할 수 있습니다.");
        }

        if (tradePost.isOwner(requesterId)) {
            throw new IllegalArgumentException("본인의 교환글에는 신청할 수 없습니다.");
        }

        Account requester = accountRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        Map<Card, Long> cardMap = checkAndUpdateCard(requesterId, dto.cardIds());

        TradeRequest tradeRequest = new TradeRequest(tradePost, requester);
        for (Map.Entry<Card, Long> entry : cardMap.entrySet()) {
            new TradeRequestCard(tradeRequest, entry.getKey(), entry.getValue());
        }

        tradeRequestRepository.save(tradeRequest);
    }

    @Transactional
    public void tradeComplete(Long postId, Long ownerId, TradeStatusRequestDto dto) {
        // 교환글 조회
        TradePost tradePost = tradePostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 교환글을 찾을 수 없습니다."));

        // 교환글 검증
        if (!tradePost.isOwner(ownerId)) {
            throw new AccessDeniedException("교환글 작성자만 교환을 완료할 수 있습니다.");
        }

        if (!tradePost.isWaiting()) {
            throw new IllegalArgumentException("대기 중인 교환글만 완료할 수 있습니다.");
        }

        // 교환 신청 조회
        TradeRequest tradeRequest = tradeRequestRepository.findById(dto.requestId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 교환 신청을 찾을 수 없습니다."));

        // 교환 신청 검증
        if (!tradeRequest.getTradePost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 교환글의 신청이 아닙니다.");
        }

        if (!tradeRequest.isWaiting()) {
            throw new IllegalStateException("대기 중인 신청만 선택할 수 있습니다.");
        }

        // 교환 실행
        complete(tradePost, tradeRequest);

        // 교환되지 않은 카드 복구
        List<TradeRequest> allRequests = tradeRequestRepository.findByTradePostAndTradeStatus(tradePost, TradeStatus.WAITING);
        allRequests.stream().filter(request -> !request.getId().equals(dto.requestId()))
                .forEach(request -> {
                    // 카드 조회
                    Map<Card, Long> requestCardMap = request.getRequesterCards().stream()
                            .collect(Collectors.toMap(TradeRequestCard::getCard, TradeRequestCard::getCount));

                    // 카드 복구
                    cardService.upsertList(request.getRequester(), requestCardMap);

                    // 상태 변경
                    request.reject();
                });
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

        Map<Card, Long> ownerCardMap = tradePost.getOwnerCards().stream()
                .collect(Collectors.toMap(TradePostCard::getCard, TradePostCard::getCount));
        Map<Card, Long> requestCardMap = tradeRequest.getRequesterCards().stream()
                .collect(Collectors.toMap(TradeRequestCard::getCard, TradeRequestCard::getCount));

        try {
            // 카드 저장
            cardService.upsertList(owner, requestCardMap);
            cardService.upsertList(requester, ownerCardMap);

            // 교환 상태 변경
            tradePost.complete();
            tradeRequest.complete();
        } catch (Exception e) {
            throw new IllegalStateException("카드 교환 중 에러가 발생했습니다.");
        }
    }
}

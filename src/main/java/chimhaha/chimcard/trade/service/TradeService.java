package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.card.repository.CardCustomRepository;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.trade.dto.TradePostCreateDto;
import chimhaha.chimcard.trade.dto.TradeRequestCreateDto;
import chimhaha.chimcard.trade.repository.TradePostRepository;
import chimhaha.chimcard.trade.repository.TradeRequestRepository;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Account requester = accountRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        Map<Card, Long> cardMap = checkAndUpdateCard(requesterId, dto.cardIds());

        TradeRequest tradeRequest = new TradeRequest(tradePost, requester);
        for (Map.Entry<Card, Long> entry : cardMap.entrySet()) {
            new TradeRequestCard(tradeRequest, entry.getKey(), entry.getValue());
        }

        tradeRequestRepository.save(tradeRequest);
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
}

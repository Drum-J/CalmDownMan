package chimhaha.chimcard.trade;

import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.querydsl.QueryDslTest;
import chimhaha.chimcard.trade.repository.TradePostRepository;
import chimhaha.chimcard.trade.repository.TradeRequestRepository;
import chimhaha.chimcard.utils.CardUtils;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static chimhaha.chimcard.entity.QAccountCard.*;

public class TradeCompleteTest extends QueryDslTest {

    @Autowired TradePostRepository tradePostRepository;
    @Autowired TradeRequestRepository tradeRequestRepository;
    @Autowired AccountCardRepository accountCardRepository;
    @Autowired EntityManager em;

    /**
     * 교환 성공 시나리오 :
     * 교환 등록자(account_id:8)가 교환 신청 목록(trade_request)에서 하나를 선택.
     * 해당 신청자와 교환 성공(COMPLETED) 나머지 신청은 실패(REJECTED).
     * 등록자와 신청자 카드를 교환 처리하고 나머지 신청자들의 카드는 돌려줌.
     */
    @Test
    @DisplayName("교환 성공")
    void tradeComplete() throws Exception {
        //given
        Long ownerId = 1L;
        Long tradePostId = 1L; // 내 교환글
        Long tradeRequestId = 1L; // 교환 신청 목록 중 하나를 선택

        //when
        TradePost tradePost = tradePostRepository.findById(tradePostId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 교환글을 찾을 수 없습니다."));

        if (!tradePost.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("교환글 작성자만 교환을 완료할 수 있습니다.");
        }

        List<TradeRequest> allRequests =
                tradeRequestRepository.findByTradePostAndTradeStatus(tradePost, TradeStatus.WAITING);

        //then
        List<TradeRequest> rejectedRequests = new ArrayList<>();
        for (TradeRequest request : allRequests) {
            if (request.getId().equals(tradeRequestId)) {
                completeTrade(tradePost, request);
            } else {
                rejectedRequests.add(request);
            }
        }

        rejectedTrade(rejectedRequests);

        em.flush();
    }

    private void completeTrade(TradePost post, TradeRequest request) {
        System.out.println("== completeTrade ==");
        Account owner = post.getOwner();
        Map<Card, Long> ownerCardMap = post.getOwnerCards().stream()
                .collect(Collectors.toMap(TradePostCard::getCard, TradePostCard::getCount));

        Account requester = request.getRequester();
        Map<Card, Long> requestCardMap = request.getRequesterCards().stream()
                .collect(Collectors.toMap(TradeRequestCard::getCard, TradeRequestCard::getCount));

        saveCard(owner, requestCardMap);
        post.complete();

        saveCard(requester, ownerCardMap);
        request.complete();
    }

    private void rejectedTrade(List<TradeRequest> rejectedRequests) {
        System.out.println("== rejectedTrade ==");
        for (TradeRequest request : rejectedRequests) {
            request.reject();

            Account account = request.getRequester();
            Map<Card, Long> cardMap = request.getRequesterCards().stream()
                    .collect(Collectors.toMap(TradeRequestCard::getCard, TradeRequestCard::getCount));

            saveCard(account, cardMap);
        }
    }

    private void saveCard(Account account, Map<Card, Long> cardMap) {
        System.out.println("== saveCard ==");
        List<AccountCard> getMyCard = query
                .selectFrom(accountCard)
                .where(accountCard.account().eq(account),
                        accountCard.card().in(cardMap.keySet()))
                .fetch();

        Map<Card, AccountCard> accountCardMap = CardUtils.accountCardMapCard(getMyCard);

        List<AccountCard> addCard = new ArrayList<>();
        for (Map.Entry<Card, Long> entry : cardMap.entrySet()) {
            Card card = entry.getKey();
            Long count = entry.getValue();

            AccountCard accountCard = accountCardMap.get(card);
            if (accountCard == null) {
                accountCard = new AccountCard(account, card, count);
            } else {
                accountCard.increaseCount(count);
            }

            addCard.add(accountCard);
        }

        accountCardRepository.saveAll(addCard);
    }
}

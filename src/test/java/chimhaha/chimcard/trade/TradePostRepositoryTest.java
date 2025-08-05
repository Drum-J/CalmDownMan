package chimhaha.chimcard.trade;

import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.entity.AccountCard;
import chimhaha.chimcard.entity.TradePost;
import chimhaha.chimcard.entity.TradePostCard;
import chimhaha.chimcard.querydsl.QueryDslTest;
import chimhaha.chimcard.trade.repository.TradePostRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static chimhaha.chimcard.entity.QAccountCard.accountCard;

public class TradePostRepositoryTest extends QueryDslTest {

    @Autowired AccountCardRepository accountCardRepository;
    @Autowired TradePostRepository tradePostRepository;
    @Autowired EntityManager em;

    @Test
    void postTest() throws Exception {
        Long traderId = 1L;
        List<Long> cardIds = List.of(1L, 3L, 5L, 5L, 6L);

        Map<Long, Long> ownerCardCounts = cardIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<AccountCard> accountCards = query
                .selectFrom(accountCard)
                .where(
                        accountCard.account().id.eq(traderId),
                        accountCard.card().id.in(ownerCardCounts.keySet())
                )
                .fetch();

        Map<Long, AccountCard> ownerCardMap = accountCards.stream().collect(
                Collectors.toMap(
                        ac -> ac.getCard().getId(),
                        Function.identity()
                ));

        // 실제 TradePost 엔티티에 데이터 추가
        TradePost tradePost = new TradePost(accountCards.getFirst().getAccount(),
                "SR 55도발로 SR 한교동 구합니다!", "저에게 제발 한교동을 주세요.");

        // 교환 신청과 동시에 개수 차감
        for (Map.Entry<Long, Long> entry : ownerCardCounts.entrySet()) {
            Long cardId = entry.getKey();
            Long count = entry.getValue();

            AccountCard ac = ownerCardMap.get(cardId);

            if (ac == null) {
                throw new IllegalArgumentException("보유하지 않은 카드입니다. cardId=" + cardId);
            }

            if (ac.decreaseCount(count)) {
                accountCardRepository.delete(ac);
            }

            new TradePostCard(tradePost, ac.getCard(), count);
        }
        tradePostRepository.save(tradePost);

        // TODO: 교환 성공 -> 상대 카드와 교환 / 교환 취소 -> 내 카드에 재등록

        em.flush();
    }
}

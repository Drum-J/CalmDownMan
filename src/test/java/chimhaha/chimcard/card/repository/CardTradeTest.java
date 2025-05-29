package chimhaha.chimcard.card.repository;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.AccountCard;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.querydsl.QueryDslTest;
import chimhaha.chimcard.user.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static chimhaha.chimcard.entity.QAccountCard.accountCard;

/** document
 * 카드 교환 로직
 * 본인이 소유한 카드 조회 API를 통해 교환에 사용할 카드를 고르고
 * 전체 카드 목록(여기서 필터링 가능)에서 교환하고자 하는 카드를 선택한다.
 * 등급, 침투력, 공격타입(가위,바위,보), 시즌 별 필터링 가능
 * (️➡️ 가능하다면 전체 카드 목록을 화면으로 내려주고 화면에서 필터링 기능을 사용)
 *
 * TODO: 현재는 AccountCard의 데이터를 조작해서 교환을 진행하고 있지만 추후 교환 관련 엔티티 생성 필요
 * 교환 엔티티를 사용할 경우 AccountCard의 데이터를 바로 변경하지 않고
 * 교환이 이뤄졌을 경우 AccountCard의 데이터를 변경하는 방식도 고려.
 *
 * 1) 교환 등록 즉시 AccountCard.count 값 변경 -> 교환 등록 후에는 해당 카드를 사용할 수 없음
 * 2) 교환 등록 후 실제 교환이 성공했을 경우 AccountCard.count 변경 -> 교환 등록 후에도 해당 카드를 사용할 수 있음
 * 
 * 1의 경우에는 교환을 취소할 경우 다시 AccountCard.count 값을 원복해줘야 하지만 2의 경우에는 처리할 필요가 없다.
 * 다만 2의 경우 교환 등록한 카드로 게임을 실행 할 경우에 문제가 될 수 있음.
 * 게임 로직은 아직 만들지 않았기 때문에 추후 더 정리할 필요가 있음
 */
public class CardTradeTest extends QueryDslTest {

    @Autowired AccountCardRepository accountCardRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired CardRepository cardRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("카드 교환 전 내가 가지고 있는 카드 처리 - 카드 많은 경우")
    void beforeCardTrade() throws Exception {
        //given
        Long accountId = 1L;
        Long[] cardIds = {2L, 24L, 24L, 24L, 67L};

        // 1. 요청 수량을 세서 Map<Long cardId, Long count> 생성 ex) {2=1, 24=3, 67=1}
        Map<Long, Long> requestedCounts = Arrays.stream(cardIds)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 2. 필요한 카드들에 대해 AccountCard를 한 번에 조회
        List<AccountCard> accountCards = query
                .selectFrom(accountCard)
                .where(
                        accountCard.account().id.eq(accountId),
                        accountCard.card().id.in(requestedCounts.keySet())
                )
                .fetch();

        // 3. 조회된 AccountCard들을 Map<cardId, AccountCard> 형태로 변환
        Map<Long, AccountCard> ownedCardMap = accountCards.stream()
                .collect(Collectors.toMap(
                        ac -> ac.getCard().getId(),
                        Function.identity()
                ));

        for (Map.Entry<Long, Long> entry : requestedCounts.entrySet()) {
            Long cardId = entry.getKey();
            Long requiredCount = entry.getValue();

            AccountCard owned = ownedCardMap.get(cardId);

            // 4. 요청한 카드 ID에 대해 보유 수량 검증
            if (owned == null || owned.getCount() < requiredCount) {
                throw new IllegalArgumentException("보유하지 않았거나 수량이 부족한 카드입니다. cardId=" + cardId);
            }

            // 5. 모든 검증이 끝났다면 수량 차감 및 삭제 처리
            if (owned.decreaseCount(requiredCount)) {
                accountCardRepository.delete(owned);
                break;
            }
        }
    }


    /**
     * 이 방법은 교환할 카드가 적은 경우 문제가 되지 않는다고 한다.
     * 한번에 교환 할 수 있는 카드 수를 적게 제한한다면 사용해도 될 것 같다.
     */
    @Test
    @DisplayName("카드 교환 전 내가 가지고 있는 카드 처리 - 카드 적은 경우")
    void beforeCardTradeSimple() throws Exception {
        //given
        Long accountId = 1L;
        Long[] cardIds = {2L, 24L, 24L, 24L, 67L};

        //when

        //then
        for (Long cardId : cardIds) {
            AccountCard findCard = query
                    .selectFrom(accountCard)
                    .where(
                            accountCard.account().id.eq(accountId),
                            accountCard.card().id.eq(cardId)
                    ).fetchOne();

            if (findCard == null || findCard.getCount() < 1) {
                throw new IllegalArgumentException("보유하지 않았거나 수량이 부족한 카드입니다. cardId=" + cardId);
            }

            if (findCard.decreaseCount()) {
                accountCardRepository.delete(findCard);
            }
        }
    }

    @Test
    void afterTrade() throws Exception {
        //given
        Long traderId = 1L; // 교환 등록자
        Long[] tradeCardIds = {2L, 24L, 24L, 24L, 67L}; // 교환 등록 카드

        Long accountId = 8L; // 교환 받을 사람
        Long[] cardIds = {6L}; //교환 해줄 카드

        // 내 계정에 등록할 상대 카드 Map
        Account myAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        Map<Long, Long> traderCardMap = Arrays.stream(tradeCardIds)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 상대 계정에 등록할 내 카드 Map
        Account traderAccount = accountRepository.findById(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        Map<Long, Long> myCardMap = Arrays.stream(cardIds)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 교환에 사용되는 모든 카드 조회
        Set<Long> cardIdSet = new HashSet<>();
        cardIdSet.addAll(myCardMap.keySet());
        cardIdSet.addAll(traderCardMap.keySet());

        List<Card> allCards = cardRepository.findAllById(cardIdSet);
        Map<Long, Card> allCardMap = allCards.stream().collect(Collectors.toMap(Card::getId, Function.identity()));

        // 두 계정의 보유 카드 Map
        Map<String, AccountCard> accountCardMap = accountCardRepository
                .findByAccountInAndCardIn(List.of(myAccount, traderAccount), allCards)
                .stream().collect(Collectors.toMap(
                        ac -> ac.getAccount().getId() + "-" + ac.getCard().getId(), Function.identity()));

        // 새로 등록할 카드 리스트
        List<AccountCard> saveList = new ArrayList<>();

        // 내 계정에 상대 카드 등록
        tradeUpdate(myAccount, traderCardMap, allCardMap, accountCardMap, saveList);
        // 상대 계정에 내 카드 등록
        tradeUpdate(traderAccount, myCardMap, allCardMap, accountCardMap, saveList);

        accountCardRepository.saveAll(saveList); // insert, update 모두 명시적으로 처리
        em.flush(); // update, insert 쿼리를 로그로 확인하기 위해 추가
    }

    private void tradeUpdate(Account account,
                             Map<Long, Long> traderCardMap, Map<Long, Card> allCardMap,
                             Map<String, AccountCard> accountCardMap, List<AccountCard> saveList) {
        for (Map.Entry<Long, Long> entry : traderCardMap.entrySet()) {
            Long cardId = entry.getKey();
            Long count = entry.getValue(); // 카드 갯수

            Card card = allCardMap.get(cardId);
            if (card == null) {
                throw new ResourceNotFoundException("해당 카드를 찾을 수 없습니다.");
            }

            String accountCardKey = account.getId() + "-" + cardId;
            AccountCard accountCard = accountCardMap.get(accountCardKey); // 이미 소유한 카드인지 확인

            if (accountCard != null) {
                accountCard.tradeCardCount(count); // 교환 갯수만큼 수량 증가
            } else {
                // map에서 accountCardKey에 해당하는 데이터가 없다면 null이 넘어옴
                // 유저가 해당 카드를 소유하고 있지 않다는 뜻
                // 새로운 accountCard 객체를 만들어서 map에 추가해줌.
                accountCard = new AccountCard(account, card, count);
                accountCardMap.put(accountCardKey, accountCard);
            }

            // 조회된 모든 accountCard 를 saveList에 담음.
            // update는 더티체킹으로 실행 되지만 saveAll()메서드를 통해 명시적으로 처리하기 위함
            saveList.add(accountCard);
        }
    }
}

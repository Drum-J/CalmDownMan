package chimhaha.chimcard.querydsl;

import chimhaha.chimcard.card.dto.MyCardDetailDto;
import chimhaha.chimcard.card.dto.QMyCardDetailDto;
import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.AccountCard;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.user.repository.AccountRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static chimhaha.chimcard.entity.QAccount.account;
import static chimhaha.chimcard.entity.QAccountCard.accountCard;
import static chimhaha.chimcard.entity.QCard.card;
import static chimhaha.chimcard.entity.QCardSeason.cardSeason;
import static com.querydsl.jpa.JPAExpressions.select;

public class AccountCardQueryTest extends QueryDslTest {

    @Autowired AccountRepository accountRepository;

    /**
    * TODO: 현재 JPA와 QueryDSL을 사용한 데이터 조회의 성능은 JPA:200ms, QueryDSL:680ms 정도로 약 3배 이상 차이난다.
    * 추후에 성능 확인 필요
    */


    @Test
    @DisplayName("accountId로 Account 조회 후 AccountCard 재조회")
    void getCardsByAccount() throws Exception {
        //given
        Long accountId = 1L;
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다.")); // 쿼리 1회 실행

        //when
        // AccountCardRepository.findByAccount() 와 같은 쿼리
        List<AccountCard> result = query
                .selectFrom(accountCard)
                .where(accountCard.account().eq(findAccount))
                .fetch(); // 쿼리 1회 실행

        //then
        // accountCard를 돌면서 MyCardDetailDto 생성
        // Card와 CardSeason의 FetchType 이 LAZY Loading 이기 때문에 stream 에서 여러개의 쿼리가 실행됨.
        List<MyCardDetailDto> list = result.stream().map(
                ac -> new MyCardDetailDto(ac.getCard(), ac.getCount())).toList(); // 쿼리 25회 실행
        // select * from card 23회 / select * from card_season 2회

        System.out.println(list);
    }

    @Test
    @DisplayName("accountId를 바로 사용해서 AccountCard 조회")
    void getCardsByAccountId() throws Exception {
        //given
        Long accountId = 1L;

        //when
        List<AccountCard> result = query.selectFrom(accountCard)
                .where(accountCard.account().id.eq(accountId))
                .fetch(); // 쿼리 1회 실행

        //then
        // accountCard를 돌면서 MyCardDetailDto 생성
        List<MyCardDetailDto> list = result.stream().map(
                ac -> new MyCardDetailDto(ac.getCard(), ac.getCount())).toList(); // 쿼리 25회 실행
        // select * from card 23회 / select * from card_season 2회

        System.out.println(list.size());
    }

    @Test
    @DisplayName("accountId로 조회 및 조회 결과를 DTO 바로 반환")
    void getCardsUseDTO() throws Exception {
        //given
        Long accountId = 1L;

        //when
        // select 에서 card와 cardSeason을 사용하기 때문에 자동으로 join을 통해 해당 값을 즉시 가져옴! -> 1개의 쿼리로 결과 모두 반환 가능
        List<MyCardDetailDto> result = query
                .select(new QMyCardDetailDto(
                        accountCard.card().id,
                        accountCard.card().title,
                        accountCard.card().attackType.stringValue(),
                        accountCard.card().grade,
                        accountCard.card().power,
                        accountCard.card().imageUrl,
                        accountCard.card().cardSeason().seasonName,
                        accountCard.count
                ))
                .from(accountCard)
                .where(accountCard.account().id.eq(accountId))
                .fetch();

        //then
        for (MyCardDetailDto dto : result) {
            System.out.println(dto);
        }
    }

    @Test
    @DisplayName("소유하지 않은 카드 조회")
    void getCardsNotMine() throws Exception {
        //given
        Long accountId = 8L;

        //when
        List<Card> result = query
                .selectFrom(card)
                .where(card.id.notIn(
                    //JPAExpressions 사용 (서브쿼리)
                    select(accountCard.card().id)
                        .from(accountCard)
                        .where(accountCard.account().id.eq(accountId))
                ), cardSeasonEq(null))
                .fetch();

        //then
        System.out.println("총 " + result.size() + "종 부족");
        for (Card c : result) {
            System.out.println(c.getId() + ": " + c.getTitle());
        }
    }

    private BooleanExpression cardSeasonEq(Long cardSeasonId) {
        return cardSeasonId != null ? cardSeason.id.eq(cardSeasonId) : null;
    }

    @Test
    @DisplayName("해당 카드를 가지고 있는 유저 조회")
    void getCardOwner() throws Exception {
        //given
        Long cardId = 6L; // 현재 DB 상 6, 24, 26, 27, 29 카드는 겹침

        //when
        List<Account> result = query
                .selectFrom(account)
                .where(account.id.in(
                    select(accountCard.account().id)
                        .from(accountCard)
                        .where(accountCard.card().id.eq(cardId))
                ))
                .fetch();

        //then
        List<String> nicknames = result.stream().map(Account::getNickname).toList();
        System.out.println(nicknames);
    }
}

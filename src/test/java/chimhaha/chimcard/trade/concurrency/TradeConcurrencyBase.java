package chimhaha.chimcard.trade.concurrency;

import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.trade.dto.TradePostCreateDto;
import chimhaha.chimcard.trade.dto.TradeRequestCreateDto;
import chimhaha.chimcard.trade.repository.TradePostRepository;
import chimhaha.chimcard.trade.repository.TradeRequestRepository;
import chimhaha.chimcard.trade.service.TradeService;
import chimhaha.chimcard.user.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class TradeConcurrencyBase {

    @Autowired
    protected TradeService tradeService;
    @Autowired
    protected AccountRepository accountRepository;
    @Autowired
    protected CardRepository cardRepository;
    @Autowired
    protected AccountCardRepository accountCardRepository;
    @Autowired
    protected TradePostRepository tradePostRepository;
    @Autowired
    protected TradeRequestRepository tradeRequestRepository;
    @Autowired
    protected EntityManager em;
    
    protected Account owner, userB, userC, userD;
    protected Card ownerCard, userBCard, userCCard, userDCard;
    protected TradePost tradePost;
    protected TradeRequest requestB, requestC, requestD;

    @BeforeEach
    void setUp() throws Exception {
        // 사용자 세팅
        owner = accountRepository.save(makeAccount("owner"));
        userB = accountRepository.save(makeAccount("userB"));
        userC = accountRepository.save(makeAccount("userC"));
        userD = accountRepository.save(makeAccount("userD"));

        // 카드 세팅
        ownerCard = cardRepository.save(makeCard("ownerCard"));
        userBCard = cardRepository.save(makeCard("userBCard"));
        userCCard = cardRepository.save(makeCard("userCCard"));
        userDCard = cardRepository.save(makeCard("userDCard"));

        // 사용자 - 카드 세팅
        accountCardRepository.save(new AccountCard(owner, ownerCard));
        accountCardRepository.save(new AccountCard(userB, userBCard));
        accountCardRepository.save(new AccountCard(userC, userCCard));
        accountCardRepository.save(new AccountCard(userD, userDCard));

        // 교환글 등록
        TradePostCreateDto postCreateDto = makeTradePostCreateDto(ownerCard.getId());
        tradeService.tradePost(owner.getId(), postCreateDto);
        tradePost = tradePostRepository.findAll().getLast();

        // user B,C,D 교환 신청
        TradeRequestCreateDto requestDtoB = makeTradeRequestCreateDto(userBCard.getId());
        TradeRequestCreateDto requestDtoC = makeTradeRequestCreateDto(userCCard.getId());
        TradeRequestCreateDto requestDtoD = makeTradeRequestCreateDto(userDCard.getId());

        tradeService.tradeRequest(tradePost.getId(), userB.getId(), requestDtoB);
        tradeService.tradeRequest(tradePost.getId(), userC.getId(), requestDtoC);
        tradeService.tradeRequest(tradePost.getId(), userD.getId(), requestDtoD);

        requestB = tradeRequestRepository.findByRequester(userB).getFirst();
        requestC = tradeRequestRepository.findByRequester(userC).getFirst();
        requestD = tradeRequestRepository.findByRequester(userD).getFirst();

        log.info("setUp() - end");
    }

    protected Account makeAccount(String username) {
        return Account.builder()
                .username(username)
                .point(0)
                .build();
    }

    protected Card makeCard(String title) {
        return Card.builder()
                .title(title)
                .attackType(AttackType.ALL)
                .grade(Grade.R)
                .power(10)
                .build();
    }

    protected TradePostCreateDto makeTradePostCreateDto(Long cardId) {
        return new TradePostCreateDto("교환글 제목", "교환글 내용", List.of(cardId));
    }

    protected TradeRequestCreateDto makeTradeRequestCreateDto(Long cardId) {
        return new TradeRequestCreateDto(List.of(cardId));
    }
}

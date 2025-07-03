package chimhaha.chimcard.trade.concurrency;

import chimhaha.chimcard.entity.TradePost;
import chimhaha.chimcard.entity.TradeRequest;
import chimhaha.chimcard.entity.TradeStatus;
import chimhaha.chimcard.trade.dto.TradeStatusRequestDto;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ActiveProfiles("h2")
public class TradeServiceConcurrencyTest extends TradeConcurrencyBase {

    @Test
    @DisplayName("사용자 B와 교환 성공 / C,D는 교환 거절 - 정상 흐름")
    void success() throws Exception {
        //given
        TradeStatusRequestDto dto = new TradeStatusRequestDto(requestB.getId());

        //when
        tradeService.tradeComplete(tradePost.getId(), owner.getId(), dto);

        //then
        // 비동기 스레드의 로직이 5초 이내로 끝나는지 확인
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                //Optional.get() 사용 시 isPresent() 체크 해야하지만 테스트라 무시
                tradeRequestRepository.findById(requestC.getId()).get().getTradeStatus().equals(TradeStatus.REJECTED) &&
                tradeRequestRepository.findById(requestD.getId()).get().getTradeStatus().equals(TradeStatus.REJECTED)
        );

        // 교환 결과 조회 .get() 무시
        TradePost completedPost = tradePostRepository.findById(tradePost.getId()).get();
        TradeRequest completedRequestB = tradeRequestRepository.findById(requestB.getId()).get();
        TradeRequest rejectedRequestC = tradeRequestRepository.findById(requestC.getId()).get();
        TradeRequest rejectedRequestD = tradeRequestRepository.findById(requestD.getId()).get();

        // 1. 교환 상태 검증
        assertAll(
                () -> assertEquals(TradeStatus.COMPLETED, completedPost.getTradeStatus()),
                () -> assertEquals(TradeStatus.COMPLETED, completedRequestB.getTradeStatus()),
                () -> assertEquals(TradeStatus.REJECTED, rejectedRequestC.getTradeStatus()),
                () -> assertEquals(TradeStatus.REJECTED, rejectedRequestD.getTradeStatus())
        );

        // 2. 카드 변경 검증
        // 2-1. owner 소유 카드 변경
        assertAll(
                () -> assertTrue(accountCardRepository.findByAccountAndCard(owner, userBCard).isPresent()),
                () -> assertFalse(accountCardRepository.findByAccountAndCard(owner, ownerCard).isPresent())
        );

        // 2-2. userB 소유 카드 변경
        assertAll(
                () -> assertTrue(accountCardRepository.findByAccountAndCard(userB,ownerCard).isPresent()),
                () -> assertFalse(accountCardRepository.findByAccountAndCard(userB, userBCard).isPresent())
        );

        // 2-3. C,D 카드 유지
        assertAll(
                () -> assertTrue(accountCardRepository.findByAccountAndCard(userC, userCCard).isPresent()),
                () -> assertTrue(accountCardRepository.findByAccountAndCard(userD, userDCard).isPresent())
        );
    }
}
